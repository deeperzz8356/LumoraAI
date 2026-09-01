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
import com.deep.lumoraai.R
import com.deep.lumoraai.core.restrictions.GenerationGate
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class BgStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val generationRepository = GenerationRepository()
    private val authRepository = AuthRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val mediaStorage = MediaStorageRepository.getInstance(application)
    private val historyRepository = HistoryRepository(
        LumoraDatabase.getInstance(application).historyDao
    )

    private var sourceImageB64: String? = null

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
            val decoded = withContext(Dispatchers.IO) {
                runCatching { decodeBitmap(uri) }.getOrNull()
            }
            if (decoded == null) {
                sourceImageB64 = null
                uiState = uiState.copy(status = BgStudioStatus.Error("Could not open that image."))
                return@launch
            }
            sourceImageB64 = withContext(Dispatchers.Default) { decoded.toJpegBase64() }
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
                        persistGeneratedImage(result.getOrThrow(), jobTitle)
                    } else {
                        val message = result.exceptionOrNull()?.message ?: "Could not replace background."
                        uiState = uiState.copy(status = BgStudioStatus.Error(message))
                        GenerationRepository.updateJob(jobTitle) { job ->
                            job.copy(progressPercent = null, statusText = "Failed", subtitle = message)
                        }
                    }
                }
            }
        }
    }

    private suspend fun persistGeneratedImage(payload: String, jobTitle: String) {
        val saved = mediaStorage.saveImageFromPayload(payload)
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
            "Keep the original subject exactly the same and replace only the background with: $prompt. Match perspective, lighting, shadows, depth of field, and reflections. Professional realistic composite."
        } else {
            "Keep the original subject exactly the same and remove the background. Place the subject on a clean neutral studio background with natural lighting and soft shadow."
        }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    private fun shortTimestamp(): String =
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
}
