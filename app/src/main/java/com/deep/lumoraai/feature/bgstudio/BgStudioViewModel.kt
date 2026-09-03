package com.deep.lumoraai.feature.bgstudio

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.BuildConfig
import com.deep.lumoraai.R
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.notification.NotificationManager
import com.deep.lumoraai.core.notification.TaskNotificationHelper
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.LumoraNotificationCenter
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.model.ActiveJobInfo
import com.deep.lumoraai.data.model.HistoryModel
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.AuthRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.HistoryRepository
import com.deep.lumoraai.data.repository.MediaStorageRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

private const val APYHUB_REMOVE_BG_URL =
    "https://api.eu.apyhub.com/apyhub/remove-background-from-images/multi-part/download"

class BgStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val generationRepository = GenerationRepository()
    private val authRepository = AuthRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val mediaStorage = MediaStorageRepository.getInstance(application)
    private val historyRepository = HistoryRepository(
        LumoraDatabase.getInstance(application).historyDao
    )
    private val notificationManager = NotificationManager(LumoraDatabase.getInstance(application).notificationDao)

    private var sourceImageB64: String? = null
    private var sourceImageUri: Uri? = null

    var uiState: BgStudioUiState by mutableStateOf(BgStudioUiState())
        private set

    fun selectMode(mode: BgStudioMode) {
        uiState = uiState.copy(mode = mode, status = BgStudioStatus.Idle)
    }

    fun updatePrompt(prompt: String) {
        uiState = uiState.copy(prompt = prompt.take(1000), status = BgStudioStatus.Idle)
    }

    fun loadImage(uri: Uri) {
        viewModelScope.launch {
            uiState = uiState.copy(status = BgStudioStatus.LoadingImage)
            val mimeType = getApplication<Application>().contentResolver.getType(uri).orEmpty()
            if (!mimeType.startsWith("image/")) {
                sourceImageB64 = null
                sourceImageUri = null
                uiState = uiState.copy(status = BgStudioStatus.Error("Upload an image file only."))
                return@launch
            }
            val decoded = withContext(Dispatchers.IO) {
                runCatching { decodeBitmap(uri) }.getOrNull()
            }
            if (decoded == null) {
                sourceImageB64 = null
                sourceImageUri = null
                uiState = uiState.copy(status = BgStudioStatus.Error("Could not open that image."))
                return@launch
            }
            sourceImageB64 = withContext(Dispatchers.Default) { decoded.toJpegBase64() }
            sourceImageUri = uri
            uiState = uiState.copy(sourceBitmap = decoded, status = BgStudioStatus.Idle)
        }
    }

    fun create() {
        val bitmap = uiState.sourceBitmap
        val source = sourceImageB64
        if (bitmap == null || source.isNullOrBlank()) {
            uiState = uiState.copy(status = BgStudioStatus.Error("Upload an image first."))
            return
        }
        if (uiState.mode == BgStudioMode.Replace && uiState.prompt.isBlank()) {
            uiState = uiState.copy(status = BgStudioStatus.Error("Describe the new background first."))
            return
        }

        viewModelScope.launch {
            if (uiState.mode == BgStudioMode.Remove) {
                startApyHubRemoveJob(bitmap)
                return@launch
            }

            val isDev = appPreferences.isDeveloperModeEnabled()
            if (!ensureTrialUser()) {
                uiState = uiState.copy(status = BgStudioStatus.Error("Could not start your free trial. Please try again."))
                return@launch
            }
            if (!isDev) {
                val credits = fetchCreditsWithSync()
                if (credits == null) {
                    uiState = uiState.copy(status = BgStudioStatus.Error("Could not verify credits. Check your connection and try again."))
                    return@launch
                }
                if (!GenerationGate.canGenerateImage(credits, isDev)) {
                    uiState = uiState.copy(status = BgStudioStatus.TrialExpired)
                    return@launch
                }
            } else {
                authRepository.syncCurrentUser()
            }

            val (width, height) = outputDimensions(bitmap)
            val prompt = buildGenerationPrompt(uiState.mode, uiState.prompt)
            startBackgroundJob(
                prompt = prompt,
                sourceImageB64 = source,
                width = width,
                height = height,
                developerMode = isDev,
            )
        }
    }

    fun resetStatus() {
        uiState = uiState.copy(status = BgStudioStatus.Idle)
    }

    private suspend fun ensureTrialUser(): Boolean =
        FirebaseAuth.getInstance().currentUser != null || authRepository.loginAnonymouslyAndSync()

    private suspend fun fetchCreditsWithSync(): Int? {
        var result = generationRepository.getCredits()
        if (result.isFailure) {
            authRepository.syncCurrentUser()
            result = generationRepository.getCredits()
        }
        return result.getOrNull()
    }

    private fun startBackgroundJob(
        prompt: String,
        sourceImageB64: String,
        width: Int,
        height: Int,
        developerMode: Boolean,
    ) {
        uiState = uiState.copy(status = BgStudioStatus.Generating)

        val taskType = if (uiState.mode == BgStudioMode.Replace) TaskNotificationHelper.BG_REPLACE else TaskNotificationHelper.BG_REMOVE
        val displayName = if (uiState.mode == BgStudioMode.Replace) "Background Replace" else "Background Remove"
        val taskId = UUID.randomUUID().toString()
        
        // Send task start notification
        viewModelScope.launch {
            notificationManager.sendTaskStartNotification(
                taskType = taskType,
                taskId = taskId,
                displayName = displayName
            )
        }

        val jobTitle = if (uiState.mode == BgStudioMode.Replace) {
            "BG Replace ${shortTimestamp()}"
        } else {
            "BG Remove ${shortTimestamp()}"
        }
        val initialJob = ActiveJobInfo(
            title = jobTitle,
            subtitle = "Replacing background...",
            badgeText = "BG Studio",
            statusText = "Queued",
            progressPercent = 0.0f,
            isCompleted = false,
            imageRes = R.drawable.style_digital,
            mediaType = MediaStorageRepository.MEDIA_IMAGE,
        )
        GenerationRepository.addJob(initialJob)

        viewModelScope.launch {
            val progressJob = launch {
                val progressSteps = listOf(
                    0.18f to "Reading subject...",
                    0.42f to "Composing new background...",
                    0.70f to "Matching light and depth...",
                    0.90f to "Finishing image..."
                )
                for (step in progressSteps) {
                    delay(1800)
                    GenerationRepository.updateJob(jobTitle) { job ->
                        job.copy(
                            progressPercent = step.first,
                            statusText = step.second,
                            subtitle = "${(step.first * 100).toInt()}% completed"
                        )
                    }
                }
            }

            GenerationRepository.runImageGeneration(
                repository = generationRepository,
                jobTitle = jobTitle,
                prompt = prompt,
                style = "Photographic",
                width = width,
                height = height,
                negativePrompt = "distorted subject, changed face, extra limbs, bad anatomy, low quality, blurry",
                sourceImageB64 = sourceImageB64,
                developerMode = developerMode,
            ) { result ->
                progressJob.cancel()
                viewModelScope.launch {
                    if (result.isSuccess) {
                        persistGeneratedImage(result.getOrThrow(), jobTitle, taskId, taskType, displayName)
                    } else {
                        val message = result.exceptionOrNull()?.message ?: "Could not replace background."
                        uiState = uiState.copy(status = BgStudioStatus.Error(message))
                        GenerationRepository.updateJob(jobTitle) { job ->
                            job.copy(progressPercent = null, statusText = "Failed", subtitle = message)
                        }
                        // Send task failure notification
                        launch {
                            notificationManager.sendTaskFailureNotification(
                                taskType = taskType,
                                taskId = taskId,
                                displayName = displayName,
                                errorMessage = message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startApyHubRemoveJob(bitmap: Bitmap) {
        val imageUri = sourceImageUri
        if (imageUri == null) {
            uiState = uiState.copy(status = BgStudioStatus.Error("Upload an image first."))
            return
        }

        uiState = uiState.copy(status = BgStudioStatus.Generating)
        
        val taskId = UUID.randomUUID().toString()
        
        // Send task start notification
        viewModelScope.launch {
            notificationManager.sendTaskStartNotification(
                taskType = TaskNotificationHelper.BG_REMOVE,
                taskId = taskId,
                displayName = "Background Remove"
            )
        }
        
        val jobTitle = "BG Remove ${shortTimestamp()}"
        GenerationRepository.addJob(
            ActiveJobInfo(
                title = jobTitle,
                subtitle = "Removing background on device...",
                badgeText = "BG Studio",
                statusText = "Processing",
                progressPercent = 0.25f,
                isCompleted = false,
                imageRes = R.drawable.style_digital,
                mediaType = MediaStorageRepository.MEDIA_IMAGE,
            )
        )

        viewModelScope.launch {
            val removedBytes = withContext(Dispatchers.IO) {
                runCatching { removeBackgroundWithApyHub(imageUri) }
            }.getOrElse { error ->
                val message = error.message ?: "Could not remove that background."
                uiState = uiState.copy(status = BgStudioStatus.Error(message))
                GenerationRepository.updateJob(jobTitle) { job ->
                    job.copy(progressPercent = null, statusText = "Failed", subtitle = message)
                }
                notificationManager.sendTaskFailureNotification(
                    taskType = TaskNotificationHelper.BG_REMOVE,
                    taskId = taskId,
                    displayName = "Background Remove",
                    errorMessage = message
                )
                return@launch
            }
            val saved = mediaStorage.saveImageBytes(removedBytes, mimeType = "image/png")
            val preview = withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(saved.filePath) }.getOrNull()
            }
            historyRepository.addHistory(
                historyModel = HistoryModel(
                    id = saved.id,
                    title = jobTitle,
                    createdAt = currentTimestamp(),
                    type = MediaStorageRepository.MEDIA_IMAGE,
                    mediaUrl = saved.filePath,
                ),
                type = MediaStorageRepository.MEDIA_IMAGE,
                mediaUrl = saved.filePath,
            )
            uiState = uiState.copy(sourceBitmap = preview ?: bitmap, status = BgStudioStatus.Completed)
            LumoraNotificationCenter.notifyCompletion(
                context = getApplication<Application>(),
                title = "Background ready",
                message = "Your background removal has finished.",
                route = Screen.History.route,
                mediaType = MediaStorageRepository.MEDIA_IMAGE,
            )
            GenerationRepository.updateJob(jobTitle) { job ->
                job.copy(
                    progressPercent = 1.0f,
                    statusText = "Completed",
                    subtitle = "Saved to history",
                    isCompleted = true,
                    localMediaPath = saved.filePath,
                    imageUrl = saved.filePath,
                )
            }
            
            // Send task complete notification
            notificationManager.sendTaskCompleteNotification(
                taskType = TaskNotificationHelper.BG_REMOVE,
                taskId = taskId,
                resultId = saved.id,
                displayName = "Background Remove"
            )
        }
    }

    private fun removeBackgroundWithApyHub(imageUri: Uri): ByteArray {
        val apiKey = BuildConfig.APYHUB_API_KEY
        if (apiKey.isBlank()) error("Background remover API key is missing.")

        val resolver = getApplication<Application>().contentResolver
        val mimeType = resolver.getType(imageUri).orEmpty()
        if (!mimeType.startsWith("image/")) error("Upload an image file only.")

        val boundary = "LumoraBoundary${UUID.randomUUID()}"
        val lineEnd = "\r\n"
        val connection = (URL("$APYHUB_REMOVE_BG_URL?output=lumora-bg-${System.currentTimeMillis()}").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("apy-token", apiKey)
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connectTimeout = 30_000
            readTimeout = 120_000
            doInput = true
            doOutput = true
        }

        return try {
            DataOutputStream(connection.outputStream).use { output ->
                output.writeBytes("--$boundary$lineEnd")
                output.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"source.${extensionForMimeType(mimeType)}\"$lineEnd")
                output.writeBytes("Content-Type: $mimeType$lineEnd$lineEnd")
                resolver.openInputStream(imageUri)?.use { input -> input.copyTo(output) }
                    ?: error("Could not open selected image.")
                output.writeBytes(lineEnd)
                output.writeBytes("--$boundary--$lineEnd")
                output.flush()
            }

            val responseCode = connection.responseCode
            val responseBytes = if (responseCode in 200..299) {
                connection.inputStream.use { it.readBytes() }
            } else {
                connection.errorStream?.use { it.readBytes() } ?: ByteArray(0)
            }
            if (responseCode !in 200..299) {
                val detail = responseBytes.toString(Charsets.UTF_8).ifBlank { "HTTP $responseCode" }
                error("Background remover failed: $detail")
            }
            if (responseBytes.isEmpty()) error("Background remover returned an empty image.")
            responseBytes
        } finally {
            connection.disconnect()
        }
    }

    private fun extensionForMimeType(mimeType: String): String =
        when (mimeType.lowercase()) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/webp" -> "webp"
            "image/png" -> "png"
            else -> "png"
        }

    private suspend fun persistGeneratedImage(payload: String, jobTitle: String, taskId: String, taskType: String, displayName: String) {
        val saved = mediaStorage.saveImageFromPayload(payload)
        val preview = withContext(Dispatchers.IO) {
            runCatching { BitmapFactory.decodeFile(saved.filePath) }.getOrNull()
        }
        historyRepository.addHistory(
            historyModel = HistoryModel(
                id = saved.id,
                title = jobTitle,
                createdAt = currentTimestamp(),
                type = MediaStorageRepository.MEDIA_IMAGE,
                mediaUrl = saved.filePath,
            ),
            type = MediaStorageRepository.MEDIA_IMAGE,
            mediaUrl = saved.filePath,
        )
        uiState = uiState.copy(sourceBitmap = preview ?: uiState.sourceBitmap, status = BgStudioStatus.Completed)
        LumoraNotificationCenter.notifyCompletion(
            context = getApplication<Application>(),
            title = "Background ready",
            message = "Your background edit has finished.",
            route = Screen.History.route,
            mediaType = MediaStorageRepository.MEDIA_IMAGE,
        )
        GenerationRepository.updateJob(jobTitle) { job ->
            job.copy(
                progressPercent = 1.0f,
                statusText = "Completed",
                subtitle = "Saved to history",
                isCompleted = true,
                localMediaPath = saved.filePath,
                imageUrl = saved.filePath,
            )
        }
        
        // Send task complete notification
        notificationManager.sendTaskCompleteNotification(
            taskType = taskType,
            taskId = taskId,
            resultId = saved.id,
            displayName = displayName
        )
    }

    private fun decodeBitmap(uri: Uri): Bitmap {
        val resolver = getApplication<Application>().contentResolver
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        } else {
            resolver.openInputStream(uri).use { input ->
                BitmapFactory.decodeStream(input) ?: error("Unsupported image file.")
            }
        }
        return bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }

    private fun Bitmap.toJpegBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 88, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun outputDimensions(bitmap: Bitmap): Pair<Int, Int> {
        val aspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        return if (aspect >= 1f) {
            1024 to (1024 / aspect).roundToInt().coerceIn(512, 1024)
        } else {
            (1024 * aspect).roundToInt().coerceIn(512, 1024) to 1024
        }
    }

    private fun buildGenerationPrompt(mode: BgStudioMode, prompt: String): String =
        if (mode == BgStudioMode.Replace) {
            "Edit the provided image. Keep the foreground subject, face, body, pose, clothing, and object details exactly the same. Replace only the background with: $prompt. Do not invent a new subject. Match perspective, lighting, shadows, depth of field, and reflections. Return a realistic composite."
        } else {
            "Edit the provided image. Keep the foreground subject exactly the same and remove only the background. Return the subject as a clean transparent PNG cutout."
        }

    private fun removeBorderConnectedBackground(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val edgeSamples = collectEdgeSamples(pixels, width, height)
        val background = averageColor(edgeSamples)
        val threshold = adaptiveBackgroundThreshold(edgeSamples, background)
        val isBackground = BooleanArray(pixels.size)
        val queue = IntArray(pixels.size)
        var head = 0
        var tail = 0

        fun enqueue(index: Int) {
            if (!isBackground[index] && colorDistance(pixels[index], background) <= threshold) {
                isBackground[index] = true
                queue[tail++] = index
            }
        }

        for (x in 0 until width) {
            enqueue(x)
            enqueue((height - 1) * width + x)
        }
        for (y in 0 until height) {
            enqueue(y * width)
            enqueue(y * width + width - 1)
        }

        while (head < tail) {
            val index = queue[head++]
            val x = index % width
            val y = index / width
            if (x > 0) enqueue(index - 1)
            if (x < width - 1) enqueue(index + 1)
            if (y > 0) enqueue(index - width)
            if (y < height - 1) enqueue(index + width)
        }

        val output = IntArray(pixels.size)
        for (i in pixels.indices) {
            val alpha = when {
                isBackground[i] -> 0
                touchesBackground(i, width, height, isBackground) -> 210
                nearBackground(i, width, height, isBackground) -> 238
                else -> android.graphics.Color.alpha(pixels[i])
            }
            output[i] = (pixels[i] and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun collectEdgeSamples(pixels: IntArray, width: Int, height: Int): IntArray {
        val samples = ArrayList<Int>((width + height) * 2)
        val step = ((width + height) / 140).coerceAtLeast(1)
        for (x in 0 until width step step) {
            samples.add(pixels[x])
            samples.add(pixels[(height - 1) * width + x])
        }
        for (y in 0 until height step step) {
            samples.add(pixels[y * width])
            samples.add(pixels[y * width + width - 1])
        }
        return samples.toIntArray()
    }

    private fun averageColor(colors: IntArray): Int {
        var r = 0L
        var g = 0L
        var b = 0L
        colors.forEach { color ->
            r += android.graphics.Color.red(color)
            g += android.graphics.Color.green(color)
            b += android.graphics.Color.blue(color)
        }
        val count = colors.size.coerceAtLeast(1)
        return android.graphics.Color.rgb((r / count).toInt(), (g / count).toInt(), (b / count).toInt())
    }

    private fun adaptiveBackgroundThreshold(edgeSamples: IntArray, background: Int): Int {
        val averageDistance = edgeSamples
            .map { colorDistance(it, background) }
            .average()
            .takeIf { !it.isNaN() }
            ?: 42.0
        return (averageDistance * 1.9).roundToInt().coerceIn(42, 96)
    }

    private fun colorDistance(first: Int, second: Int): Int {
        val redMean = (android.graphics.Color.red(first) + android.graphics.Color.red(second)) / 2
        val red = android.graphics.Color.red(first) - android.graphics.Color.red(second)
        val green = android.graphics.Color.green(first) - android.graphics.Color.green(second)
        val blue = android.graphics.Color.blue(first) - android.graphics.Color.blue(second)
        val weighted = (((512 + redMean) * red * red) shr 8) + 4 * green * green + (((767 - redMean) * blue * blue) shr 8)
        return kotlin.math.sqrt(weighted.toDouble()).roundToInt()
    }

    private fun touchesBackground(index: Int, width: Int, height: Int, background: BooleanArray): Boolean {
        val x = index % width
        val y = index / width
        return (x > 0 && background[index - 1]) ||
            (x < width - 1 && background[index + 1]) ||
            (y > 0 && background[index - width]) ||
            (y < height - 1 && background[index + width])
    }

    private fun nearBackground(index: Int, width: Int, height: Int, background: BooleanArray): Boolean {
        val x = index % width
        val y = index / width
        for (dy in -2..2) {
            val ny = y + dy
            if (ny !in 0 until height) continue
            for (dx in -2..2) {
                val nx = x + dx
                if (nx !in 0 until width) continue
                if (background[ny * width + nx]) return true
            }
        }
        return false
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    private fun shortTimestamp(): String =
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
}
