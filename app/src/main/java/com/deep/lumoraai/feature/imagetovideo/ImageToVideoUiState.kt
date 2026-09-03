package com.deep.lumoraai.feature.imagetovideo

import android.graphics.Bitmap
import com.deep.lumoraai.feature.createhub.model.VideoEngine
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.imagetoimage.VideoStyle

data class ImageToVideoUiState(
    val sourceBitmap: Bitmap? = null,
    val prompt: String = "",
    val negativePrompt: String = "",
    val selectedStyle: VideoStyle = VideoStyle.CinematicFilm,
    val selectedEngine: VideoEngine = VideoEngine.FAST_DRAFT,
    val aspectRatio: GenerationAspectRatio = GenerationAspectRatio.Story,
    val similarity: Float = 0.5f,
    val duration: Int = 5,
    val generations: Int = 1,
    val isGenerating: Boolean = false,
    val isImprovingPrompt: Boolean = false,
    val generatedPath: String? = null,
    val generatedPaths: List<String> = emptyList(),
    val generatedMimeType: String = "video/mp4",
    val error: String? = null,
)
