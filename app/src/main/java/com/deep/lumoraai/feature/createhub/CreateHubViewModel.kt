package com.deep.lumoraai.feature.createhub

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.LumoraNotificationCenter
import com.deep.lumoraai.core.utils.LocalCreditBalance
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.model.ActiveJobInfo
import com.deep.lumoraai.data.model.HistoryModel
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.AuthRepository
import com.deep.lumoraai.data.repository.FakeRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.HistoryRepository
import com.deep.lumoraai.data.repository.MediaStorageRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateHubViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FakeRepository()
    private val generationRepository = GenerationRepository()
    private val authRepository = AuthRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val mediaStorage = MediaStorageRepository.getInstance(application)
    private val historyRepository = HistoryRepository(
        LumoraDatabase.getInstance(application).historyDao
    )

    var uiState: CreateHubUiState by mutableStateOf(CreateHubUiState.Loading)
        private set

    init { load() }

    fun load() {
        val items = when ("createhub") {
            "templates" -> repository.getTemplates().map { it.title }
            "history" -> repository.getHistory().map { it.title }
            "credits" -> repository.getCredits().map { "${it.label}: ${it.amount}" }
            "notifications" -> repository.getNotifications().map { it.title }
            "queue" -> repository.getQueue().map { it.title }
            "result" -> repository.getResults().map { it.title }
            "profile" -> listOf(repository.getProfile().name, repository.getProfile().plan, "${repository.getProfile().credits} credits")
            else -> listOf("Create Hub ready", "Fake data only", "No Firebase, AI, Room, Retrofit, or network")
        }
        uiState = if (items.isEmpty()) CreateHubUiState.Empty else CreateHubUiState.Success(items)
    }

    fun generateImage(
        prompt: String,
        style: String,
        width: Int = 1024,
        height: Int = 1024,
        negativePrompt: String? = null,
        sourceImageB64: String? = null
    ) {
        if (prompt.isBlank()) {
            uiState = CreateHubUiState.Error("Prompt cannot be empty")
            return
        }

        viewModelScope.launch {
            val isDev = appPreferences.isDeveloperModeEnabled()
            if (!ensureTrialUser()) {
                uiState = CreateHubUiState.Error("Could not start your free trial. Please try again.")
                return@launch
            }
            if (!isDev) {
                val credits = fetchCreditsWithSync()
                if (credits == null) {
                    uiState = CreateHubUiState.Error("Could not verify credits. Check your connection and try again.")
                    return@launch
                }
                if (!GenerationGate.canGenerateImage(credits, isDev)) {
                    uiState = CreateHubUiState.TrialExpired
                    return@launch
                }
            } else {
                authRepository.syncCurrentUser()
            }
            startImageGeneration(
                prompt = prompt,
                style = style,
                width = width,
                height = height,
                negativePrompt = negativePrompt,
                sourceImageB64 = sourceImageB64,
                developerMode = isDev,
            )
        }
    }

    private suspend fun fetchCreditsWithSync(): Int? {
        var result = generationRepository.getCredits()
        if (result.isFailure) {
            authRepository.syncCurrentUser()
            result = generationRepository.getCredits()
        }
        return LocalCreditBalance.maxWith(getApplication(), result.getOrNull())
    }

    private suspend fun ensureTrialUser(): Boolean {
        return if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
            true
        } else {
            authRepository.loginAnonymouslyAndSync()
        }
    }

    private fun startImageGeneration(
        prompt: String,
        style: String,
        width: Int,
        height: Int,
        negativePrompt: String?,
        sourceImageB64: String?,
        developerMode: Boolean,
    ) {
        uiState = CreateHubUiState.Generating

        val jobTitle = prompt
        val initialJob = ActiveJobInfo(
            title = jobTitle,
            subtitle = "Est. 12s remaining",
            badgeText = "Text to Image",
            statusText = "Queued",
            progressPercent = 0.0f,
            isCompleted = false,
            imageRes = com.deep.lumoraai.R.drawable.style_anime,
            mediaType = MediaStorageRepository.MEDIA_IMAGE,
        )
        GenerationRepository.addJob(initialJob)

        viewModelScope.launch {
            val progressJob = launch {
                val progressSteps = listOf(
                    0.1f to "Connecting to Render...",
                    0.25f to "Allocating GPU...",
                    0.50f to "Synthesizing latent space...",
                    0.80f to "Decoding frames..."
                )
                for (step in progressSteps) {
                    delay(2000)
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
                style = style,
                width = width,
                height = height,
                negativePrompt = negativePrompt,
                sourceImageB64 = sourceImageB64,
                developerMode = developerMode,
            ) { result ->
                progressJob.cancel()
                viewModelScope.launch {
                    if (result.isSuccess) {
                        persistGeneratedMedia(
                            payload = result.getOrThrow(),
                            prompt = prompt,
                            mediaType = MediaStorageRepository.MEDIA_IMAGE,
                            jobTitle = jobTitle,
                            badgeText = "Text to Image",
                        )
                    } else {
                        val message = result.exceptionOrNull()?.message
                            ?: "Failed to generate image. Please try again."
                        uiState = CreateHubUiState.Error(message)
                        GenerationRepository.updateJob(jobTitle) { job ->
                            job.copy(
                                progressPercent = null,
                                statusText = "Failed",
                                subtitle = message
                            )
                        }
                    }
                }
            }
        }
    }

    fun generateVideo(
        prompt: String,
        engine: String,
        sourceImageB64: String? = null,
        motionStrength: Int = 65,
        cameraDirection: String? = null,
        duration: Int = 10
    ) {
        if (uiState is CreateHubUiState.Generating) return
        if (prompt.isBlank()) {
            uiState = CreateHubUiState.Error("Prompt cannot be empty")
            return
        }

        viewModelScope.launch {
            val isDev = appPreferences.isDeveloperModeEnabled()
            if (!ensureTrialUser()) {
                uiState = CreateHubUiState.Error("Could not start your free trial. Please try again.")
                return@launch
            }
            if (!isDev) {
                val credits = fetchCreditsWithSync()
                if (credits == null) {
                    uiState = CreateHubUiState.Error("Could not verify credits. Check your connection and try again.")
                    return@launch
                }
                if (!GenerationGate.canGenerateVideo(credits, isDev)) {
                    uiState = CreateHubUiState.TrialExpired
                    return@launch
                }
            }
            uiState = CreateHubUiState.Generating

            val jobTitle = prompt
            val initialJob = ActiveJobInfo(
                title = jobTitle,
                subtitle = "Est. 60s remaining",
                badgeText = "Text to Video",
                statusText = "Queued",
                progressPercent = 0.1f,
                isCompleted = false,
                imageRes = com.deep.lumoraai.R.drawable.style_anime,
                mediaType = MediaStorageRepository.MEDIA_VIDEO,
            )
            GenerationRepository.addJob(initialJob)

            val result = generationRepository.generateVideo(
                prompt = prompt,
                engine = engine,
                sourceImageB64 = sourceImageB64,
                motionStrength = motionStrength,
                cameraDirection = cameraDirection,
                duration = duration,
                developerMode = isDev,
            )
            if (result.isSuccess) {
                persistGeneratedMedia(
                    payload = result.getOrThrow(),
                    prompt = prompt,
                    mediaType = MediaStorageRepository.MEDIA_VIDEO,
                    jobTitle = jobTitle,
                    badgeText = "Text to Video",
                )
            } else {
                val message = result.exceptionOrNull()?.message ?: "Failed to generate video."
                uiState = CreateHubUiState.Error(message)
                GenerationRepository.updateJob(jobTitle) { job ->
                    job.copy(
                        progressPercent = null,
                        statusText = "Failed",
                        subtitle = message,
                    )
                }
            }
        }
    }

    private suspend fun persistGeneratedMedia(
        payload: String,
        prompt: String,
        mediaType: String,
        jobTitle: String,
        badgeText: String,
    ) {
        try {
            val saved = if (mediaType == MediaStorageRepository.MEDIA_VIDEO) {
                mediaStorage.saveVideoFromPayload(payload)
            } else {
                mediaStorage.saveImageFromPayload(payload)
            }

            historyRepository.addHistory(
                historyModel = HistoryModel(
                    id = saved.id,
                    title = prompt,
                    createdAt = currentTimestamp(),
                    type = mediaType,
                    mediaUrl = saved.filePath,
                ),
                type = mediaType,
                mediaUrl = saved.filePath,
            )

            if (mediaType == MediaStorageRepository.MEDIA_VIDEO) {
                uiState = CreateHubUiState.VideoGenerated(saved.filePath, saved.mimeType)
            } else {
                uiState = CreateHubUiState.ImageGenerated(saved.filePath, saved.mimeType)
            }
            LumoraNotificationCenter.notifyCompletion(
                context = getApplication<Application>(),
                title = if (mediaType == MediaStorageRepository.MEDIA_VIDEO) "Video ready" else "Image ready",
                message = "Your $badgeText creation has finished.",
                route = Screen.History.route,
                mediaType = mediaType,
            )

            GenerationRepository.updateJob(jobTitle) { job ->
                job.copy(
                    progressPercent = 1.0f,
                    statusText = "Completed",
                    subtitle = "Saved to device",
                    isCompleted = true,
                    badgeText = badgeText,
                    mediaType = mediaType,
                    localMediaPath = saved.filePath,
                    imageUrl = if (mediaType == MediaStorageRepository.MEDIA_IMAGE) saved.filePath else job.imageUrl,
                    videoUrl = if (mediaType == MediaStorageRepository.MEDIA_VIDEO) saved.filePath else null,
                )
            }
        } catch (e: Exception) {
            val message = e.message ?: "Generated media could not be saved."
            uiState = CreateHubUiState.Error(message)
            GenerationRepository.updateJob(jobTitle) { job ->
                job.copy(
                    progressPercent = null,
                    statusText = "Failed",
                    subtitle = message,
                )
            }
        }
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
}
