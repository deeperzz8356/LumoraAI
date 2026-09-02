package com.deep.lumoraai.feature.texttovideo

import com.deep.lumoraai.feature.createhub.model.VideoEngine
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.imagetoimage.VideoStyle

data class TextToVideoUiState(
    val title: String = "Text 2 Video",
    val promptHint: String = "Describe the video you want to generate...",
    val jobBadge: String = "Text 2 Video",
    val prompt: String = "",
    val negativePrompt: String = "",
    val selectedStyle: VideoStyle = VideoStyle.CinematicFilm,
    val selectedEngine: VideoEngine = VideoEngine.FAST_DRAFT,
    val aspectRatio: GenerationAspectRatio = GenerationAspectRatio.Story,
    val motion: Float = 0.5f,
    val duration: Int = 5,
    val generations: Int = 2,
    val isGenerating: Boolean = false,
    val isImprovingPrompt: Boolean = false,
    val generatedPath: String? = null,
    val generatedMimeType: String = "video/mp4",
    val error: String? = null,
)
