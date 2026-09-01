package com.deep.lumoraai.feature.texttoimage

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
import com.deep.lumoraai.feature.imagetoimage.ImageStyle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TextToImageViewModel(application: Application) : AndroidViewModel(application) {

    private val generationRepository = GenerationRepository()
    private val authRepository = AuthRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val mediaStorage = MediaStorageRepository.getInstance(application)
    private val historyRepository = HistoryRepository(LumoraDatabase.getInstance(application).historyDao)

    var uiState: TextToImageUiState by mutableStateOf(TextToImageUiState())
        private set

    fun applyTemplatePrompt(prompt: String?) {
        if (prompt.isNullOrBlank()) return
        uiState = uiState.copy(prompt = prompt.take(1000), error = null, generatedPath = null)
    }

    fun updatePrompt(prompt: String) {
        uiState = uiState.copy(prompt = prompt.take(1000), error = null)
    }

    fun updateNegativePrompt(prompt: String) {
        uiState = uiState.copy(negativePrompt = prompt.take(1000), error = null)
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

    fun generate() {
        if (uiState.isGenerating) return
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
                if (!GenerationGate.canGenerateImage(credits, isDev)) {
                    uiState = uiState.copy(error = GenerationGate.insufficientCreditsMessage())
                    return@launch
                }
            } else {
                authRepository.syncCurrentUser()
            }
            startImageJob(developerMode = isDev)
        }
    }

    fun improvePrompt() {
        if (uiState.isImprovingPrompt || uiState.prompt.isBlank()) return

        viewModelScope.launch {
            uiState = uiState.copy(isImprovingPrompt = true, error = null)
            val result = generationRepository.enhancePrompt(
                prompt = uiState.prompt,
                mediaType = "IMAGE",
                style = uiState.selectedStyle.label,
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
        uiState = uiState.copy(generatedPath = null)
    }

    private fun startImageJob(developerMode: Boolean) {
        val prompt = buildPrompt()
        val jobTitle = "Text 2 Image ${shortTimestamp()}"
        uiState = uiState.copy(isGenerating = true, error = null)
        GenerationRepository.addJob(
            ActiveJobInfo(
                title = jobTitle,
                subtitle = "Generating image...",
                badgeText = "Text 2 Image",
                statusText = "Queued",
                progressPercent = 0.0f,
                isCompleted = false,
                imageRes = R.drawable.style_anime,
                mediaType = MediaStorageRepository.MEDIA_IMAGE,
            )
        )

        viewModelScope.launch {
            val progressJob = launch {
                val steps = listOf(
                    0.18f to "Reading prompt...",
                    0.42f to "Composing style...",
                    0.68f to "Rendering details...",
                    0.90f to "Finishing image..."
                )
                for (step in steps) {
                    delay(1800)
                    GenerationRepository.updateJob(jobTitle) { job ->
                        job.copy(progressPercent = step.first, statusText = step.second, subtitle = "${(step.first * 100).toInt()}% completed")
                    }
                }
            }

            GenerationRepository.runImageGeneration(
                repository = generationRepository,
                jobTitle = jobTitle,
                prompt = prompt,
                style = uiState.selectedStyle.label,
                width = 1024,
                height = 1024,
                negativePrompt = uiState.negativePrompt.ifBlank { "low quality, blurry, distorted face, extra limbs, bad anatomy, watermark, text artifacts" },
                sourceImageB64 = null,
                developerMode = developerMode,
            ) { result ->
                progressJob.cancel()
                viewModelScope.launch {
                    if (result.isSuccess) {
                        persistGeneratedImage(result.getOrThrow(), jobTitle, prompt)
                    } else {
                        val message = result.exceptionOrNull()?.message ?: "Could not generate image."
                        uiState = uiState.copy(isGenerating = false, error = message)
                        GenerationRepository.updateJob(jobTitle) { job ->
                            job.copy(progressPercent = null, statusText = "Failed", subtitle = message)
                        }
                    }
                }
            }
        }
    }

    private suspend fun persistGeneratedImage(payload: String, jobTitle: String, prompt: String) {
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
        uiState = uiState.copy(isGenerating = false, generatedPath = saved.filePath, generatedMimeType = saved.mimeType)
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
        val creativity = (uiState.creativity * 100).toInt()
        return "${uiState.prompt}. Style: ${uiState.selectedStyle.label} (${uiState.selectedStyle.promptHint}). Creativity level: $creativity%."
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    private fun shortTimestamp(): String =
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
}
