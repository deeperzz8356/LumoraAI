package com.deep.lumoraai.feature.texttovideo

import android.app.Application
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
import com.deep.lumoraai.feature.createhub.model.VideoEngine
import com.deep.lumoraai.feature.imagetoimage.ImageStyle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TextToVideoViewModel(application: Application) : AndroidViewModel(application) {

    private val generationRepository = GenerationRepository()
    private val authRepository = AuthRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val mediaStorage = MediaStorageRepository.getInstance(application)
    private val historyRepository = HistoryRepository(LumoraDatabase.getInstance(application).historyDao)
    private var isPromoMode = false

    var uiState: TextToVideoUiState by mutableStateOf(TextToVideoUiState())
        private set

    fun configure(isPromo: Boolean, initialPrompt: String? = null) {
        val modeChanged = isPromoMode != isPromo
        isPromoMode = isPromo
        val modeState = if (isPromo) {
            uiState.copy(
                title = "Promo Video",
                promptHint = "Describe the product, offer, or ad video you want...",
                jobBadge = "Promo Video",
                error = null,
                generatedPath = null,
            )
        } else {
            uiState.copy(
                title = "Text 2 Video",
                promptHint = "Describe the video you want to generate...",
                jobBadge = "Text 2 Video",
                error = null,
                generatedPath = null,
            )
        }
        uiState = if (!initialPrompt.isNullOrBlank()) {
            modeState.copy(prompt = initialPrompt.take(1000), error = null, generatedPath = null)
        } else if (modeChanged) {
            modeState
        } else {
            uiState
        }
    }

    fun updatePrompt(prompt: String) {
        uiState = uiState.copy(prompt = prompt.take(1000), error = null)
    }

    fun selectStyle(style: ImageStyle) {
        uiState = uiState.copy(selectedStyle = style)
    }

    fun selectEngine(engine: VideoEngine) {
        uiState = uiState.copy(selectedEngine = engine)
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
            uiState = uiState.copy(error = if (isPromoMode) "Describe the promo video you want to create." else "Describe the video you want to generate.")
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
                if (!GenerationGate.canGenerateVideo(credits, isDev)) {
                    uiState = uiState.copy(error = GenerationGate.insufficientCreditsMessage())
                    return@launch
                }
            }

            uiState = uiState.copy(isGenerating = true, error = null)
            val prompt = buildPrompt()
            val jobTitle = "${uiState.jobBadge} ${shortTimestamp()}"
            GenerationRepository.addJob(
                ActiveJobInfo(
                    title = jobTitle,
                    subtitle = "Generating video...",
                    badgeText = uiState.jobBadge,
                    statusText = "Queued",
                    progressPercent = 0.1f,
                    isCompleted = false,
                    imageRes = R.drawable.style_digital,
                    mediaType = MediaStorageRepository.MEDIA_VIDEO,
                )
            )

            val result = generationRepository.generateVideo(
                prompt = prompt,
                engine = uiState.selectedEngine.modelId,
                sourceImageB64 = null,
                motionStrength = (uiState.motion * 100).toInt().coerceIn(20, 90),
                duration = uiState.duration,
                developerMode = isDev,
            )

            if (result.isSuccess) {
                persistGeneratedVideo(result.getOrThrow(), jobTitle, prompt)
            } else {
                val message = result.exceptionOrNull()?.message ?: "Failed to generate video."
                uiState = uiState.copy(isGenerating = false, error = message)
                GenerationRepository.updateJob(jobTitle) { job ->
                    job.copy(progressPercent = null, statusText = "Failed", subtitle = message)
                }
            }
        }
    }

    fun dismissError() {
        uiState = uiState.copy(error = null)
    }

    fun clearResult() {
        uiState = uiState.copy(generatedPath = null)
    }

    private suspend fun persistGeneratedVideo(payload: String, jobTitle: String, prompt: String) {
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
        uiState = uiState.copy(isGenerating = false, generatedPath = saved.filePath, generatedMimeType = saved.mimeType)
        GenerationRepository.updateJob(jobTitle) { job ->
            job.copy(
                progressPercent = 1.0f,
                statusText = "Completed",
                subtitle = "Saved to device",
                isCompleted = true,
                localMediaPath = saved.filePath,
                videoUrl = saved.filePath,
            )
        }
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

    private fun buildPrompt(): String {
        val motion = (uiState.motion * 100).toInt()
        val base = if (isPromoMode) {
            "Create a polished promotional video for this product, service, or offer. Include strong visual hooks, clear marketing pacing, premium lighting, and ad-ready composition."
        } else {
            "Create a cinematic text-to-video scene with natural motion, camera movement, depth, and coherent subject action."
        }
        return "$base User prompt: ${uiState.prompt}. Style: ${uiState.selectedStyle.label}. Motion strength: $motion%."
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    private fun shortTimestamp(): String =
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
}
