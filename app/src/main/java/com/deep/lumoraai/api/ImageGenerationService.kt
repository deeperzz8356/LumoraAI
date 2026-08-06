package com.deep.lumoraai.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Service for calling the backend image and video generation APIs.
 * All calls require Firebase authentication token.
 */
interface ImageGenerationService {

    /**
     * Generate an image using Vertex AI Imagen model
     * 
     * @param request The image generation request
     * @return The generated image data
     */
    @POST("/api/v1/images/generate")
    suspend fun generateImage(@Body request: ImageGenerateRequest): ImageGenerateResponse

    /**
     * Generate a video using Vertex AI Veo model
     * 
     * @param request The video generation request
     * @return The generated video URL/data
     */
    @POST("/api/v1/videos/generate")
    suspend fun generateVideo(@Body request: VideoGenerateRequest): VideoGenerateResponse
}

/**
 * Request to generate an image from text prompt
 */
data class ImageGenerateRequest(
    @SerializedName("prompt")
    val prompt: String,

    @SerializedName("width")
    val width: Int = 1024,

    @SerializedName("height")
    val height: Int = 1024,

    @SerializedName("model")
    val model: String = "imagen-3.0-generate-002",

    @SerializedName("seed")
    val seed: Int? = null,

    @SerializedName("negative_prompt")
    val negativePrompt: String? = null
)

/**
 * Response containing generated image data
 */
data class ImageGenerateResponse(
    @SerializedName("image_bytes")
    val imageBytes: String, // Base64 encoded image data

    @SerializedName("mime_type")
    val mimeType: String, // e.g., "image/png"

    @SerializedName("model")
    val model: String // Model used for generation
)

/**
 * Request to generate a video from text prompt
 */
data class VideoGenerateRequest(
    @SerializedName("prompt")
    val prompt: String,

    @SerializedName("aspect_ratio")
    val aspectRatio: String = "16:9", // Supported: 16:9, 9:16, 1:1

    @SerializedName("duration")
    val duration: Int = 8, // Seconds, typically 8-150

    @SerializedName("model")
    val model: String = "veo-2.0-generate-001",

    @SerializedName("source_image_b64")
    val sourceImageB64: String? = null // Optional: Base64 encoded source image for transformation

)

/**
 * Response containing generated video URL or data
 */
data class VideoGenerateResponse(
    @SerializedName("status")
    val status: String, // "success" or "error"

    @SerializedName("video_url")
    val videoUrl: String? = null, // URL or data URL of generated video

    @SerializedName("model")
    val model: String? = null, // Model used

    @SerializedName("provider")
    val provider: String? = null, // "vertex-ai", "cloudflare", etc.

    @SerializedName("error")
    val error: String? = null // Error message if status is "error"
)
