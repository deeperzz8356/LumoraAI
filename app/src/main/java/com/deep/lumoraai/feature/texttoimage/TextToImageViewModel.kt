package com.deep.lumoraai.feature.texttoimage

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.R
import com.deep.lumoraai.core.notification.NotificationManager
import com.deep.lumoraai.core.notification.TaskNotificationHelper
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.utils.CreditBalanceStore
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
import com.deep.lumoraai.feature.imagetoimage.ImageStyle
import com.deep.lumoraai.feature.imagetoimage.apiStyle
import com.deep.lumoraai.feature.imagetoimage.promptDirective
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TextToImageViewModel(application: Application) : AndroidViewModel(application) {

    private val generationRepository = GenerationRepository()
    private val authRepository = AuthRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val mediaStorage = MediaStorageRepository.getInstance(application)
    private val historyRepository = HistoryRepository(LumoraDatabase.getInstance(application).historyDao)
    private val notificationManager = NotificationManager(LumoraDatabase.getInstance(application).notificationDao, application)

    var uiState: TextToImageUiState by mutableStateOf(TextToImageUiState())
        private set

    fun applyTemplatePrompt(prompt: String?) {
        if (prompt.isNullOrBlank()) return
        uiState = uiState.copy(prompt = prompt.take(1000), error = null, generatedPath = null, generatedPaths = emptyList())
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

    fun selectModel(model: ImageModel) {
        uiState = uiState.copy(selectedModel = model)
    }

    fun setCreativity(value: Float) {
        uiState = uiState.copy(creativity = value.coerceIn(0f, 1f))
    }

    fun setGenerations(value: Int) {
        uiState = uiState.copy(generations = value.coerceIn(1, 4))
    }

    fun setAspectRatio(value: GenerationAspectRatio) {
        uiState = uiState.copy(aspectRatio = value)
    }

    fun generate(mode: TextToImageMode = TextToImageMode.TextToImage) {
        if (uiState.isGenerating) return
        if (uiState.prompt.isBlank()) {
            uiState = uiState.copy(error = mode.emptyPromptError())
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
            // Optimistic: drop the header instantly by the expected cost
            // (1 credit per image). The post-completion refresh reconciles to
            // the authoritative server balance (and undoes this if it failed).
            if (!isDev) {
                CreditBalanceStore.applyOptimistic(-GenerationGate.CREDITS_PER_IMAGE * requestedGenerationsFor())
            }
            startImageJobs(mode = mode, developerMode = isDev)
        }
    }

    private fun requestedGenerationsFor(): Int = uiState.generations.coerceIn(1, 4)

    fun improvePrompt(mode: TextToImageMode = TextToImageMode.TextToImage) {
        if (uiState.isImprovingPrompt || uiState.prompt.isBlank()) return

        viewModelScope.launch {
            uiState = uiState.copy(isImprovingPrompt = true, error = null)
            val result = generationRepository.enhancePrompt(
                prompt = mode.promptEnhancerSeed(uiState.prompt),
                mediaType = "IMAGE",
                style = mode.apiStyle(uiState.selectedStyle),
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

    private fun startImageJobs(mode: TextToImageMode, developerMode: Boolean) {
        val prompt = buildPrompt(mode)
        val requestedGenerations = uiState.generations.coerceIn(1, 4)
            uiState = uiState.copy(
                isGenerating = true,
                generationProgress = 0f,
                generationStatusText = "${mode.displayName} 1 of $requestedGenerations generating",
                error = null,
                generatedPath = null,
                generatedPaths = emptyList()
            )

        viewModelScope.launch {
            var completed = 0
            repeat(requestedGenerations) { index ->
                uiState = uiState.copy(
                    generationProgress = 0f,
                    generationStatusText = "${mode.displayName} ${index + 1} of $requestedGenerations generating"
                )
                val jobTitle = "${mode.displayName} ${shortTimestamp()} #${index + 1}"
                val taskId = UUID.randomUUID().toString()
                notificationManager.sendTaskStartNotification(
                    taskType = TaskNotificationHelper.TEXT_TO_IMAGE,
                    taskId = taskId,
                    displayName = mode.displayName
                )
                GenerationRepository.addJob(
                    ActiveJobInfo(
                        title = jobTitle,
                        subtitle = "Generating ${mode.displayName.lowercase()} ${index + 1} of $requestedGenerations...",
                        badgeText = mode.displayName,
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
                    style = mode.apiStyle(uiState.selectedStyle),
                    width = uiState.aspectRatio.width,
                    height = uiState.aspectRatio.height,
                    negativePrompt = mode.negativePrompt(uiState.negativePrompt).take(API_PROMPT_LIMIT),
                    sourceImageB64 = null,
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
                        taskType = TaskNotificationHelper.TEXT_TO_IMAGE,
                        taskId = taskId,
                        displayName = mode.displayName,
                        errorMessage = message
                    )
                    return@launch
                }
            }
            uiState = uiState.copy(isGenerating = false, generationProgress = null, generationStatusText = null, error = null)
            // Re-fetch the authoritative balance so the header reflects the
            // credits the server deducted for this batch.
            if (completed > 0) CreditBalanceStore.refresh()
            if (completed > 1) {
                LumoraNotificationCenter.notifyCompletion(
                    context = getApplication<Application>(),
                    title = "$completed ${mode.outputPlural} ready",
                    message = "Your ${mode.displayName} batch has finished.",
                    route = Screen.History.route,
                    mediaType = MediaStorageRepository.MEDIA_IMAGE,
                )
            }
        }
    }

    private fun launchProgressJob(jobTitle: String, current: Int, total: Int) = viewModelScope.launch {
        val steps = listOf(
            0.18f to "Reading prompt...",
            0.42f to "Composing style...",
            0.68f to "Rendering details...",
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
            message = "Your Text 2 Image creation has finished.",
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
            taskType = TaskNotificationHelper.TEXT_TO_IMAGE,
            taskId = taskId,
            resultId = saved.id,
            displayName = "Text to Image"
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
        // Gate on the SERVER balance only. Using max(server, local) let a stale
        // local value pass the affordability check even when the server would
        // reject, producing a confusing start-then-fail. The server is the
        // single source of truth for spendable credits.
        val credits = result.getOrNull()
        CreditBalanceStore.set(credits)
        return credits
    }

    private fun buildPrompt(mode: TextToImageMode): String {
        val creativity = (uiState.creativity * 100).toInt()
        val stylePrompt = if (mode == TextToImageMode.TextToImage) uiState.selectedStyle.promptDirective else ""
        return when (mode) {
            TextToImageMode.TextToImage -> buildApiPrompt(
                prefix = "",
                userPrompt = uiState.prompt,
                suffix = ".$stylePrompt Format: ${uiState.aspectRatio.promptHint}. Creativity level: $creativity%."
            )

            TextToImageMode.Logo -> buildApiPrompt(
                prefix = "Logo for: ",
                userPrompt = uiState.prompt,
                suffix = ". Make it a real logo: simple iconic mark or mascot emblem, clean vector-like shapes, centered 1:1 composition, minimal background, no mockup or poster scene. Creativity $creativity%."
            )

            TextToImageMode.Avatar -> buildApiPrompt(
                prefix = "Avatar for: ",
                userPrompt = uiState.prompt,
                suffix = ". Make it a profile avatar: centered head-and-shoulders character or mascot portrait, clear face/identity, clean background, social profile ready. Creativity $creativity%."
            )
        }
    }

    private fun buildApiPrompt(prefix: String, userPrompt: String, suffix: String): String {
        val normalizedPrompt = userPrompt.trim().replace(Regex("\\s+"), " ")
        val overhead = prefix.length + suffix.length
        val availablePromptLength = (API_PROMPT_LIMIT - overhead).coerceAtLeast(32)
        return (prefix + normalizedPrompt.take(availablePromptLength).trimEnd() + suffix).take(API_PROMPT_LIMIT)
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    private fun shortTimestamp(): String =
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
}

private const val API_PROMPT_LIMIT = 500

private val TextToImageMode.displayName: String
    get() = when (this) {
        TextToImageMode.TextToImage -> "Text 2 Image"
        TextToImageMode.Logo -> "Logo"
        TextToImageMode.Avatar -> "Avatar"
    }

private val TextToImageMode.outputPlural: String
    get() = when (this) {
        TextToImageMode.TextToImage -> "images"
        TextToImageMode.Logo -> "logos"
        TextToImageMode.Avatar -> "avatars"
    }

private fun TextToImageMode.emptyPromptError(): String =
    when (this) {
        TextToImageMode.TextToImage -> "Describe the image you want to generate."
        TextToImageMode.Logo -> "Describe the logo you want to generate."
        TextToImageMode.Avatar -> "Describe the avatar you want to generate."
    }

private fun TextToImageMode.apiStyle(selectedStyle: ImageStyle): String =
    when (this) {
        TextToImageMode.TextToImage -> selectedStyle.apiStyle
        TextToImageMode.Logo -> "logo"
        TextToImageMode.Avatar -> "avatar"
    }

private fun TextToImageMode.promptEnhancerSeed(prompt: String): String =
    when (this) {
        TextToImageMode.TextToImage -> prompt
        TextToImageMode.Logo -> "Create a professional logo concept for: $prompt"
        TextToImageMode.Avatar -> "Create a polished avatar concept for: $prompt"
    }

private fun TextToImageMode.negativePrompt(userNegativePrompt: String): String {
    val defaults = when (this) {
        TextToImageMode.TextToImage -> "low quality, blurry, distorted face, extra limbs, bad anatomy, watermark, text artifacts"
        TextToImageMode.Logo -> "photorealistic scene, poster, flyer, mockup, busy background, complex environment, tiny unreadable details, watermark, text artifacts, low quality, blurry"
        TextToImageMode.Avatar -> "full body distant shot, landscape scene, busy background, distorted face, extra limbs, bad anatomy, watermark, text artifacts, low quality, blurry"
    }
    return userNegativePrompt.ifBlank { defaults }
}


