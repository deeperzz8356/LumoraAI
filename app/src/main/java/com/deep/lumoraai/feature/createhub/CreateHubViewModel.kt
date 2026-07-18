package com.deep.lumoraai.feature.createhub

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.repository.FakeRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.model.ActiveJobInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CreateHubViewModel(
    private val repository: FakeRepository = FakeRepository(),
    private val generationRepository: GenerationRepository = GenerationRepository()
) : ViewModel() {
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
        
        uiState = CreateHubUiState.Generating

        val jobTitle = prompt
        val initialJob = ActiveJobInfo(
            title = jobTitle,
            subtitle = "Est. 12s remaining",
            badgeText = "Text to Image",
            statusText = "Queued",
            progressPercent = 0.0f,
            isCompleted = false,
            imageRes = com.deep.lumoraai.R.drawable.style_anime
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

            try {
                val imageUrl = generationRepository.generateImage(
                    prompt = prompt, 
                    style = style, 
                    width = width, 
                    height = height,
                    negativePrompt = negativePrompt,
                    sourceImageB64 = sourceImageB64
                )
                progressJob.cancel()
                if (imageUrl != null) {
                    uiState = CreateHubUiState.ImageGenerated(imageUrl)
                    GenerationRepository.updateJob(jobTitle) { job ->
                        job.copy(
                            progressPercent = 1.0f,
                            statusText = "Completed",
                            subtitle = "Finished just now",
                            isCompleted = true,
                            imageUrl = imageUrl
                        )
                    }
                } else {
                    uiState = CreateHubUiState.Error("Failed to generate image. Please try again.")
                    GenerationRepository.updateJob(jobTitle) { job ->
                        job.copy(
                            progressPercent = null,
                            statusText = "Failed",
                            subtitle = "Generation failed"
                        )
                    }
                }
            } catch (e: Exception) {
                progressJob.cancel()
                uiState = CreateHubUiState.Error(e.message ?: "An error occurred")
                GenerationRepository.updateJob(jobTitle) { job ->
                    job.copy(
                        progressPercent = null,
                        statusText = "Failed",
                        subtitle = e.message ?: "An error occurred"
                    )
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
        if (prompt.isBlank()) {
            uiState = CreateHubUiState.Error("Prompt cannot be empty")
            return
        }

        uiState = CreateHubUiState.Generating
        viewModelScope.launch {
            val result = generationRepository.generateVideo(
                prompt = prompt, 
                engine = engine,
                sourceImageB64 = sourceImageB64,
                motionStrength = motionStrength,
                cameraDirection = cameraDirection,
                duration = duration
            )
            if (result.isSuccess) {
                uiState = CreateHubUiState.VideoGenerated(result.getOrNull() ?: "")
            } else {
                uiState = CreateHubUiState.Error(result.exceptionOrNull()?.message ?: "Failed to generate video.")
            }
        }
    }
}