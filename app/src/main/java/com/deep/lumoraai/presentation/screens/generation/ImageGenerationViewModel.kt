package com.deep.lumoraai.presentation.screens.generation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.api.ImageGenerateResponse
import com.deep.lumoraai.api.VideoGenerateResponse
import com.deep.lumoraai.data.repository.ImageGenerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ImageGenerationVM"

data class GenerationUiState(
    val prompt: String = "",
    val isGenerating: Boolean = false,
    val generatedImage: Bitmap? = null,
    val generatedVideoUrl: String? = null,
    val error: String? = null,
    val model: String = "imagen-3.0-generate-002",
    val width: Int = 1024,
    val height: Int = 1024,
    val seed: Int? = null
)

@HiltViewModel
class ImageGenerationViewModel @Inject constructor(
    private val imageGenerationRepository: ImageGenerationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerationUiState())
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt, error = null) }
    }

    fun updateDimensions(width: Int, height: Int) {
        _uiState.update { it.copy(width = width, height = height) }
    }

    fun updateSeed(seed: Int?) {
        _uiState.update { it.copy(seed = seed) }
    }

    /**
     * Generate an image from the current prompt
     */
    fun generateImage() {
        val currentState = _uiState.value

        // Validate prompt
        if (currentState.prompt.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a prompt") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            try {
                Log.d(TAG, "Starting image generation for: ${currentState.prompt}")

                val response = imageGenerationRepository.generateImage(
                    prompt = currentState.prompt,
                    width = currentState.width,
                    height = currentState.height,
                    seed = currentState.seed
                )

                // Decode base64 image to Bitmap
                val bitmap = decodeBase64ToBitmap(response.imageBytes)

                _uiState.update {
                    it.copy(
                        generatedImage = bitmap,
                        model = response.model,
                        isGenerating = false,
                        error = null
                    )
                }
                Log.d(TAG, "Image generation successful")
            } catch (e: Exception) {
                Log.e(TAG, "Image generation failed", e)
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = e.message ?: "Failed to generate image"
                    )
                }
            }
        }
    }

    /**
     * Generate a video from the current prompt
     */
    fun generateVideo(aspectRatio: String = "16:9", duration: Int = 8) {
        val currentState = _uiState.value

        if (currentState.prompt.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a prompt") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            try {
                Log.d(TAG, "Starting video generation for: ${currentState.prompt}")

                val response = imageGenerationRepository.generateVideo(
                    prompt = currentState.prompt,
                    aspectRatio = aspectRatio,
                    duration = duration,
                    sourceImageB64 = null
                )

                if (response.status == "success") {
                    _uiState.update {
                        it.copy(
                            generatedVideoUrl = response.videoUrl,
                            model = response.model ?: it.model,
                            isGenerating = false,
                            error = null
                        )
                    }
                    Log.d(TAG, "Video generation successful")
                } else {
                    throw Exception(response.error ?: "Video generation failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Video generation failed", e)
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = e.message ?: "Failed to generate video"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearGenerated() {
        _uiState.update {
            it.copy(
                generatedImage = null,
                generatedVideoUrl = null,
                error = null
            )
        }
    }

    /**
     * Decode base64 string to Bitmap
     */
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                ?: throw Exception("Failed to decode image bytes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode base64 image", e)
            throw Exception("Failed to process image", e)
        }
    }
}
