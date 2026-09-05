package com.deep.lumoraai.feature.texttovideo

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.R
import com.deep.lumoraai.core.notification.NotificationManager
import com.deep.lumoraai.core.notification.TaskNotificationHelper
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.CreditBalanceStore
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.model.ActiveJobInfo
import com.deep.lumoraai.data.model.HistoryModel
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.AuthRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.HistoryRepository
import com.deep.lumoraai.data.repository.MediaStorageRepository
import com.deep.lumoraai.feature.createhub.model.VideoEngine
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.imagetoimage.VideoStyle
import com.deep.lumoraai.feature.imagetoimage.apiStyle
import com.deep.lumoraai.feature.imagetoimage.promptDirective
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TextToVideoViewModel(application: Application) : AndroidViewModel(application) {
    private fun s(@StringRes id: Int, vararg args: Any): String =
        getApplication<Application>().getString(id, *args)

    private val generationRepository = GenerationRepository()
    private val authRepository = AuthRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val mediaStorage = MediaStorageRepository.getInstance(application)
    private val historyRepository = HistoryRepository(LumoraDatabase.getInstance(application).historyDao)
    private val notificationManager = NotificationManager(LumoraDatabase.getInstance(application).notificationDao, application)
    private var isPromoMode = false

    var uiState: TextToVideoUiState by mutableStateOf(TextToVideoUiState())
        private set

    fun configure(isPromo: Boolean, initialPrompt: String? = null) {
        val modeChanged = isPromoMode != isPromo
        isPromoMode = isPromo
        val modeState = if (isPromo) {
            uiState.copy(
                title = s(R.string.ui_promo_videos),
                promptHint = s(R.string.describe_promo_video),
                jobBadge = s(R.string.ui_promo_videos),
                error = null,
                generatedPath = null,
                generatedPaths = emptyList(),
            )
        } else {
            uiState.copy(
                title = s(R.string.ui_text_2_video),
                promptHint = s(R.string.describe_video_to_generate),
                jobBadge = s(R.string.ui_text_2_video),
                error = null,
                generatedPath = null,
                generatedPaths = emptyList(),
            )
        }
        uiState = if (!initialPrompt.isNullOrBlank()) {
            modeState.copy(prompt = initialPrompt.take(1000), error = null, generatedPath = null, generatedPaths = emptyList())
        } else if (modeChanged) {
            modeState
        } else {
            uiState
        }
    }

    fun updatePrompt(prompt: String) {
        uiState = uiState.copy(prompt = prompt.take(1000), error = null, generatedPath = null, generatedPaths = emptyList())
    }

    fun updateNegativePrompt(prompt: String) {
        uiState = uiState.copy(negativePrompt = prompt.take(1000), error = null, generatedPath = null, generatedPaths = emptyList())
    }

    fun selectStyle(style: VideoStyle) {
        uiState = uiState.copy(selectedStyle = style)
    }

    fun selectEngine(engine: VideoEngine) {
        uiState = uiState.copy(selectedEngine = engine)
    }

    fun setAspectRatio(value: GenerationAspectRatio) {
        uiState = uiState.copy(aspectRatio = value)
    }

    fun setMotion(value: Float) {
        uiState = uiState.copy(motion = value.coerceIn(0f, 1f))
    }

    fun setDuration(value: Int) {
        uiState = uiState.copy(duration = value.coerceIn(5, 15))
    }

    fun setGenerations(value: Int) {
        uiState = uiState.copy(generations = value.coerceIn(1, 4))
    }

    fun generate() {
        if (uiState.isGenerating) return
        if (uiState.prompt.isBlank()) {
            uiState = uiState.copy(error = s(if (isPromoMode) R.string.describe_promo_video_to_create else R.string.describe_video_to_generate))
            return
        }

        viewModelScope.launch {
            val isDev = appPreferences.isDeveloperModeEnabled()
            if (!ensureTrialUser()) {
                uiState = uiState.copy(error = s(R.string.free_trial_start_failed))
                return@launch
            }
            if (!isDev) {
                val credits = fetchCreditsWithSync()
                if (credits == null) {
                    uiState = uiState.copy(error = s(R.string.credits_verification_failed))
                    return@launch
                }
                if (!GenerationGate.canGenerateVideo(credits, isDev, uiState.generations)) {
                    uiState = uiState.copy(error = GenerationGate.insufficientCreditsMessage())
                    return@launch
                }
            }

            val taskType = if (isPromoMode) TaskNotificationHelper.PROMO_VIDEO else TaskNotificationHelper.TEXT_TO_VIDEO
            val displayName = if (isPromoMode) s(R.string.ui_promo_videos) else s(R.string.ui_text_to_video)
            val requestedGenerations = uiState.generations.coerceIn(1, 4)
            // Optimistic: drop the header instantly by the expected cost
            // (5 credits per video). Reconciled by the post-completion refresh.
            if (!isDev) {
                CreditBalanceStore.applyOptimistic(-GenerationGate.CREDITS_PER_VIDEO * requestedGenerations)
            }
            uiState = uiState.copy(
                isGenerating = true,
                generationProgress = 0.1f,
                generationStatusText = s(R.string.video_generation_progress, 1, requestedGenerations),
                error = null,
                generatedPath = null,
                generatedPaths = emptyList()
            )
            val prompt = buildPrompt()

            var completed = 0
            repeat(requestedGenerations) { index ->
                uiState = uiState.copy(
                    generationProgress = 0.1f,
                    generationStatusText = s(R.string.video_generation_progress, index + 1, requestedGenerations)
                )
                val taskId = UUID.randomUUID().toString()
                val jobTitle = "${uiState.jobBadge} ${shortTimestamp()} #${index + 1}"
                notificationManager.sendTaskStartNotification(
                    taskType = taskType,
                    taskId = taskId,
                    displayName = displayName
                )
                GenerationRepository.addJob(
                    ActiveJobInfo(
                        title = jobTitle,
                        subtitle = "Generating video ${index + 1} of $requestedGenerations...",
                        badgeText = uiState.jobBadge,
                        statusText = "Queued",
                        progressPercent = 0.1f,
                        isCompleted = false,
                        imageRes = R.drawable.style_digital,
                        mediaType = MediaStorageRepository.MEDIA_VIDEO,
                    )
                )
                val progressJob = launchProgressJob(jobTitle, index + 1, requestedGenerations)
                val result = generationRepository.generateVideo(
                    prompt = prompt,
                    engine = uiState.selectedEngine.modelId,
                    sourceImageB64 = null,
                    motionStrength = (uiState.motion * 100).toInt().coerceIn(20, 90),
                    duration = uiState.duration,
                    aspectRatio = uiState.aspectRatio.label,
                    style = uiState.selectedStyle.apiStyle,
                    developerMode = isDev,
                )
                progressJob.cancel()

                if (result.isSuccess) {
                    persistGeneratedVideo(result.getOrThrow(), jobTitle, prompt, taskId, taskType, displayName, keepGenerating = index < requestedGenerations - 1)
                    completed += 1
                } else {
                    val message = result.exceptionOrNull()?.message ?: s(R.string.video_generation_failed)
                    uiState = uiState.copy(isGenerating = false, generationProgress = null, generationStatusText = null, error = message)
                    GenerationRepository.updateJob(jobTitle) { job ->
                        job.copy(progressPercent = null, statusText = "Failed", subtitle = message)
                    }
                    notificationManager.sendTaskFailureNotification(
                        taskType = taskType,
                        taskId = taskId,
                        displayName = displayName,
                        errorMessage = message
                    )
                    return@launch
                }
            }
            uiState = uiState.copy(isGenerating = false, generationProgress = null, generationStatusText = null, error = null)
            // Re-fetch the authoritative balance so the header reflects the
            // credits the server deducted for this generation.
            if (completed > 0) CreditBalanceStore.refresh()
        }
    }

    fun improvePrompt() {
        if (uiState.isImprovingPrompt || uiState.prompt.isBlank()) return

        viewModelScope.launch {
            uiState = uiState.copy(isImprovingPrompt = true, error = null)
            val result = generationRepository.enhancePrompt(
                prompt = uiState.prompt,
                mediaType = "VIDEO",
                style = uiState.selectedStyle.apiStyle,
                negativePrompt = uiState.negativePrompt,
            )
            uiState = if (result.isSuccess) {
                uiState.copy(prompt = result.getOrThrow().take(1000), isImprovingPrompt = false, error = null)
            } else {
                uiState.copy(
                    isImprovingPrompt = false,
                    error = result.exceptionOrNull()?.message ?: s(R.string.prompt_improvement_failed)
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

    private fun launchProgressJob(jobTitle: String, current: Int, total: Int) = viewModelScope.launch {
        val steps = listOf(
            0.22f to "Reading prompt...",
            0.45f to "Planning motion...",
            0.68f to "Rendering frames...",
            0.90f to "Encoding video..."
        )
        for (step in steps) {
            delay(2400)
            uiState = uiState.copy(
                generationProgress = step.first,
                generationStatusText = s(R.string.video_generation_progress, current, total)
            )
            GenerationRepository.updateJob(jobTitle) { job ->
                job.copy(progressPercent = step.first, statusText = step.second, subtitle = "${(step.first * 100).toInt()}% completed")
            }
        }
    }

    private suspend fun persistGeneratedVideo(payload: String, jobTitle: String, prompt: String, taskId: String, taskType: String, displayName: String, keepGenerating: Boolean = false) {
        val saved = mediaStorage.saveVideoFromPayload(payload)
        historyRepository.addHistory(
            historyModel = HistoryModel(
                id = saved.id,
                title = prompt,
                createdAt = currentTimestamp(),
                type = MediaStorageRepository.MEDIA_VIDEO,
                mediaUrl = saved.filePath,
            ),
            type = MediaStorageRepository.MEDIA_VIDEO,
            mediaUrl = saved.filePath,
        )
        uiState = uiState.copy(
            isGenerating = keepGenerating,
            generationProgress = if (keepGenerating) 1f else null,
            generatedPath = saved.filePath,
            generatedPaths = uiState.generatedPaths + saved.filePath,
            generatedMimeType = saved.mimeType
        )
        GenerationRepository.updateJob(jobTitle) { job ->
            job.copy(
                progressPercent = 1.0f,
                statusText = s(R.string.completed),
                subtitle = s(R.string.saved_to_device),
                isCompleted = true,
                localMediaPath = saved.filePath,
                videoUrl = saved.filePath,
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

    private suspend fun ensureTrialUser(): Boolean =
        FirebaseAuth.getInstance().currentUser != null || authRepository.loginAnonymouslyAndSync()

    private suspend fun fetchCreditsWithSync(): Int? {
        var result = generationRepository.getCredits()
        if (result.isFailure) {
            authRepository.syncCurrentUser()
            result = generationRepository.getCredits()
        }
        // Gate on the SERVER balance only (see TextToImageViewModel for rationale).
        val credits = result.getOrNull()
        CreditBalanceStore.set(credits)
        return credits
    }

    private fun buildPrompt(): String {
        val motion = (uiState.motion * 100).toInt()
        val base = if (isPromoMode) {
            "Create a polished promotional video for this product, service, or offer. Include strong visual hooks, clear marketing pacing, premium lighting, and ad-ready composition."
        } else {
            "Create a cinematic text-to-video scene with natural motion, camera movement, depth, and coherent subject action."
        }
        val negative = uiState.negativePrompt.takeIf { it.isNotBlank() }?.let { " Avoid: $it." }.orEmpty()
        val stylePrompt = uiState.selectedStyle.promptDirective
        return "$base User prompt: ${uiState.prompt}.$stylePrompt Format: ${uiState.aspectRatio.promptHint}. Motion strength: $motion%.$negative"
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    private fun shortTimestamp(): String =
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
}

