package com.deep.lumoraai.feature.imagetovideo

import android.graphics.Bitmap
import com.deep.lumoraai.feature.createhub.model.VideoEngine
import com.deep.lumoraai.feature.imagetoimage.ImageStyle

data class ImageToVideoUiState(
    val sourceBitmap: Bitmap? = null,
    val prompt: String = "",
    val selectedStyle: ImageStyle = ImageStyle.All,
    val selectedEngine: VideoEngine = VideoEngine.FAST_DRAFT,
    val similarity: Float = 0.5f,
    val duration: Int = 5,
    val generations: Int = 2,
    val isGenerating: Boolean = false,
    val generatedPath: String? = null,
    val generatedMimeType: String = "video/mp4",
    val error: String? = null,
)

