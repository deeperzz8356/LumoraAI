package com.deep.lumoraai.feature.imagetoimage

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
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.notification.NotificationManager
import com.deep.lumoraai.core.notification.TaskNotificationHelper
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.LocalCreditBalance
import com.deep.lumoraai.core.utils.LumoraNotificationCenter
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.model.ActiveJobInfo
import com.deep.lumoraai.data.model.HistoryModel
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.AuthRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.HistoryRepository
import com.deep.lumoraai.data.repository.MediaStorageRepository
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ImageToImageViewModel(application: Application) : AndroidViewModel(application) {

    private val generationRepository = GenerationRepository()
    private val authRepository = AuthRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val mediaStorage = MediaStorageRepository.getInstance(application)
    private val historyRepository = HistoryRepository(LumoraDatabase.getInstance(application).historyDao)
    private val notificationManager = NotificationManager(LumoraDatabase.getInstance(application).notificationDao, application)
    private var sourceImageB64: String? = null

    var uiState: ImageToImageUiState by mutableStateOf(ImageToImageUiState())
        private set

    fun loadImage(uri: Uri) {
        viewModelScope.launch {
            uiState = uiState.copy(isGenerating = true, error = null, generatedPath = null, generatedPaths = emptyList())
            val decoded = withContext(Dispatchers.IO) {
                runCatching { decodeBitmap(uri) }.getOrNull()
            }
            if (decoded == null) {
                sourceImageB64 = null
                uiState = uiState.copy(isGenerating = false, error = "Could not open that image.")
                return@launch
            }
            sourceImageB64 = withContext(Dispatchers.Default) { decoded.toJpegBase64() }
            uiState = uiState.copy(sourceBitmap = decoded, isGenerating = false, error = null)
        }
    }

    fun updatePrompt(prompt: String) {
        uiState = uiState.copy(prompt = prompt.take(1000), error = null, generatedPath = null, generatedPaths = emptyList())
    }

    fun updateNegativePrompt(prompt: String) {
        uiState = uiState.copy(negativePrompt = prompt.take(1000), error = null, generatedPath = null, generatedPaths = emptyList())
    }

    fun selectStyle(style: ImageStyle) {
        uiState = uiState.copy(selectedStyle = style)
    }

    fun setSimilarity(value: Float) {
        uiState = uiState.copy(similarity = value.coerceIn(0f, 1f))
    }

    fun setGenerations(value: Int) {
        uiState = uiState.copy(generations = value.coerceIn(1, 4))
    }

    fun setAspectRatio(value: GenerationAspectRatio) {
        uiState = uiState.copy(aspectRatio = value)
    }

    fun generate() {
        val source = sourceImageB64
        if (uiState.sourceBitmap == null || source.isNullOrBlank()) {
            uiState = uiState.copy(error = "Upload an image first.")
            return
        }
        if (uiState.prompt.isBlank()) {
            uiState = uiState.copy(error = "Describe the image you want to generate.")
            return
        }

        viewModelScope.launch {
            val isDev = appPreferences.isDeveloperModeEnabled()
            if (!ensureTrialUser()) {
                uiState = uiState.copy(error = "Could not start your free trial. Please try again.")
                return@launch
            }
            if (!isDev) {
                val credits = fetchCreditsWithSync()
                if (credits == null) {
                    uiState = uiState.copy(error = "Could not verify credits. Check your connection and try again.")
                    return@launch
                }
                if (!GenerationGate.canGenerateImage(credits, isDev, uiState.generations)) {
                    uiState = uiState.copy(error = GenerationGate.insufficientCreditsMessage())
                    return@launch
                }
            } else {
                authRepository.syncCurrentUser()
            }
            startImageJobs(source, isDev)
        }
    }

    fun improvePrompt() {
        if (uiState.isImprovingPrompt || uiState.prompt.isBlank()) return

        viewModelScope.launch {
            uiState = uiState.copy(isImprovingPrompt = true, error = null)
            val result = generationRepository.enhancePrompt(
                prompt = uiState.prompt,
                mediaType = "IMAGE",
                style = uiState.selectedStyle.apiStyle,
                negativePrompt = uiState.negativePrompt,
            )
            uiState = if (result.isSuccess) {
                uiState.copy(prompt = result.getOrThrow().take(1000), isImprovingPrompt = false, error = null)
            } else {
                uiState.copy(
                    isImprovingPrompt = false,
                    error = result.exceptionOrNull()?.message ?: "Could not improve prompt."
                )
            }
        }
    }

    fun dismissError() {
        uiState = uiState.copy(error = null)
    }

    fun clearResult() {
        uiState = uiState.copy(generatedPath = null, generatedPaths = emptyList())
    }

    private fun startImageJobs(sourceImage: String, developerMode: Boolean) {
        val prompt = buildPrompt()
        val requestedGenerations = uiState.generations.coerceIn(1, 4)
        uiState = uiState.copy(
            isGenerating = true,
            generationProgress = 0f,
            generationStatusText = "Image 1 of $requestedGenerations generating",
            error = null,
            generatedPath = null,
            generatedPaths = emptyList()
        )

        viewModelScope.launch {
            var completed = 0
            repeat(requestedGenerations) { index ->
                uiState = uiState.copy(
                    generationProgress = 0f,
                    generationStatusText = "Image ${index + 1} of $requestedGenerations generating"
                )
                val jobTitle = "Image 2 Image ${shortTimestamp()} #${index + 1}"
                val taskId = UUID.randomUUID().toString()
                notificationManager.sendTaskStartNotification(
                    taskType = TaskNotificationHelper.IMAGE_TO_IMAGE,
                    taskId = taskId,
                    displayName = "Image to Image"
                )
                GenerationRepository.addJob(
                    ActiveJobInfo(
                        title = jobTitle,
                        subtitle = "Generating variation ${index + 1} of $requestedGenerations...",
                        badgeText = "Image 2 Image",
                        statusText = "Queued",
                        progressPercent = 0.0f,
                        isCompleted = false,
                        imageRes = R.drawable.style_anime,
                        mediaType = MediaStorageRepository.MEDIA_IMAGE,
                    )
                )
                val progressJob = launchProgressJob(jobTitle, index + 1, requestedGenerations)
                val result = generationRepository.generateImage(
                    prompt = prompt,
                    style = uiState.selectedStyle.apiStyle,
                    width = uiState.aspectRatio.width,
                    height = uiState.aspectRatio.height,
                    negativePrompt = uiState.negativePrompt.ifBlank { "low quality, blurry, distorted face, extra limbs, bad anatomy" },
                    sourceImageB64 = sourceImage,
                    developerMode = developerMode,
                )
                progressJob.cancel()
                if (result.isSuccess) {
                    persistGeneratedImage(result.getOrThrow(), jobTitle, prompt, taskId, keepGenerating = index < requestedGenerations - 1)
                    completed += 1
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Could not generate image."
                    uiState = uiState.copy(isGenerating = false, generationProgress = null, generationStatusText = null, error = message)
                    GenerationRepository.updateJob(jobTitle) { job ->
                        job.copy(progressPercent = null, statusText = "Failed", subtitle = message)
                    }
                    notificationManager.sendTaskFailureNotification(
                        taskType = TaskNotificationHelper.IMAGE_TO_IMAGE,
                        taskId = taskId,
                        displayName = "Image to Image",
                        errorMessage = message
                    )
                    return@launch
                }
            }
            uiState = uiState.copy(isGenerating = false, generationProgress = null, generationStatusText = null, error = null)
            if (completed > 1) {
                LumoraNotificationCenter.notifyCompletion(
                    context = getApplication<Application>(),
                    title = "$completed images ready",
                    message = "Your Image 2 Image batch has finished.",
                    route = Screen.History.route,
                    mediaType = MediaStorageRepository.MEDIA_IMAGE,
                )
            }
        }
    }

    private fun launchProgressJob(jobTitle: String, current: Int, total: Int) = viewModelScope.launch {
        val steps = listOf(
            0.18f to "Reading source image...",
            0.42f to "Applying style direction...",
            0.70f to "Balancing similarity...",
            0.90f to "Finishing image..."
        )
        for (step in steps) {
            delay(1800)
            uiState = uiState.copy(
                generationProgress = step.first,
                generationStatusText = "Image $current of $total generating"
            )
            GenerationRepository.updateJob(jobTitle) { job ->
                job.copy(progressPercent = step.first, statusText = step.second, subtitle = "${(step.first * 100).toInt()}% completed")
            }
        }
    }

    private suspend fun persistGeneratedImage(payload: String, jobTitle: String, prompt: String, taskId: String, keepGenerating: Boolean = false) {
        val saved = mediaStorage.saveImageFromPayload(payload)
        historyRepository.addHistory(
            historyModel = HistoryModel(
                id = saved.id,
                title = prompt,
                createdAt = currentTimestamp(),
                type = MediaStorageRepository.MEDIA_IMAGE,
                mediaUrl = saved.filePath,
            ),
            type = MediaStorageRepository.MEDIA_IMAGE,
            mediaUrl = saved.filePath,
        )
        uiState = uiState.copy(
            isGenerating = keepGenerating,
            generationProgress = if (keepGenerating) 1f else null,
            generatedPath = saved.filePath,
            generatedPaths = uiState.generatedPaths + saved.filePath,
            generatedMimeType = saved.mimeType
        )
        LumoraNotificationCenter.notifyCompletion(
            context = getApplication<Application>(),
            title = "Image ready",
            message = "Your Image 2 Image creation has finished.",
            route = Screen.History.route,
            mediaType = MediaStorageRepository.MEDIA_IMAGE,
        )
        GenerationRepository.updateJob(jobTitle) { job ->
            job.copy(
                progressPercent = 1.0f,
                statusText = "Completed",
                subtitle = "Saved to device",
                isCompleted = true,
                localMediaPath = saved.filePath,
                imageUrl = saved.filePath,
            )
        }
        
        // Send task complete notification
        notificationManager.sendTaskCompleteNotification(
            taskType = TaskNotificationHelper.IMAGE_TO_IMAGE,
            taskId = taskId,
            resultId = saved.id,
            displayName = "Image to Image"
        )
    }

    private suspend fun ensureTrialUser(): Boolean =
        FirebaseAuth.getInstance().currentUser != null || authRepository.loginAnonymouslyAndSync()

    private suspend fun fetchCreditsWithSync(): Int? {
        var result = generationRepository.getCredits()
        if (result.isFailure) {
            authRepository.syncCurrentUser()
            result = generationRepository.getCredits()
        }
        return LocalCreditBalance.maxWith(getApplication(), result.getOrNull())
    }

    private fun buildPrompt(): String {
        val similarity = (uiState.similarity * 100).toInt()
        val stylePrompt = uiState.selectedStyle.promptDirective
        return "Create a new image from the uploaded source. User prompt: ${uiState.prompt}.$stylePrompt Format: ${uiState.aspectRatio.promptHint}. Preserve about $similarity% of the original composition and subject identity while improving the image."
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

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    private fun shortTimestamp(): String =
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
}


