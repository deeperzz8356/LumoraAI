package com.deep.lumoraai.data.repository

import android.util.Log
import com.deep.lumoraai.api.ImageGenerateRequest
import com.deep.lumoraai.api.ImageGenerateResponse
import com.deep.lumoraai.api.ImageGenerationService
import com.deep.lumoraai.api.VideoGenerateRequest
import com.deep.lumoraai.api.VideoGenerateResponse
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ImageGenerationRepository"

/**
 * Repository for image and video generation operations.
 * Handles API communication with the backend.
 */
@Singleton
class ImageGenerationRepository @Inject constructor(
    private val imageGenerationService: ImageGenerationService
) {

    /**
     * Generate an image from a text prompt
     * 
     * @param prompt The text description of the image to generate
     * @param width Image width in pixels (default 1024)
     * @param height Image height in pixels (default 1024)
     * @param seed Optional seed for reproducibility
     * @param negativePrompt Optional negative prompt to avoid certain elements
     * @return The generated image as base64 encoded data
     * @throws Exception If authentication fails or API call fails
     */
    suspend fun generateImage(
        prompt: String,
        width: Int = 1024,
        height: Int = 1024,
        seed: Int? = null,
        negativePrompt: String? = null
    ): ImageGenerateResponse {
        return try {
            Log.d(TAG, "Generating image with prompt: $prompt")
            
            // Ensure user is authenticated
            ensureAuthenticated()

            val request = ImageGenerateRequest(
                prompt = prompt,
                width = width,
                height = height,
                seed = seed,
                negativePrompt = negativePrompt
            )

            val response = imageGenerationService.generateImage(request)
            Log.d(TAG, "Image generation successful. Model: ${response.model}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Image generation failed", e)
            throw e
        }
    }

    /**
     * Generate a video from a text prompt
     * 
     * @param prompt The text description of the video to generate
     * @param aspectRatio Video aspect ratio (16:9, 9:16, or 1:1)
     * @param duration Video duration in seconds
     * @param sourceImageB64 Optional source image for transformation (base64 encoded)
     * @return The generated video URL or data
     * @throws Exception If authentication fails or API call fails
     */
    suspend fun generateVideo(
        prompt: String,
        aspectRatio: String = "16:9",
        duration: Int = 8,
        sourceImageB64: String? = null
    ): VideoGenerateResponse {
        return try {
            Log.d(TAG, "Generating video with prompt: $prompt")

            // Ensure user is authenticated
            ensureAuthenticated()

            val request = VideoGenerateRequest(
                prompt = prompt,
                aspectRatio = aspectRatio,
                duration = duration,
                sourceImageB64 = sourceImageB64
            )

            val response = imageGenerationService.generateVideo(request)
            if (response.status == "success") {
                Log.d(TAG, "Video generation successful. Model: ${response.model}")
            } else {
                Log.e(TAG, "Video generation failed: ${response.error}")
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "Video generation API call failed", e)
            throw e
        }
    }

    /**
     * Ensures the user is authenticated with Firebase
     * and has a valid ID token for backend API calls.
     * 
     * @throws Exception If user is not authenticated or token retrieval fails
     */
    private suspend fun ensureAuthenticated() {
        val firebaseUser = Firebase.auth.currentUser
            ?: throw IllegalStateException("User is not authenticated. Please sign in first.")

        try {
            // Refresh token to ensure it's valid
            val tokenResult = firebaseUser.getIdToken(false).await()
            Log.d(TAG, "Firebase token is valid")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firebase token", e)
            throw IllegalStateException("Failed to authenticate with Firebase", e)
        }
    }
}
