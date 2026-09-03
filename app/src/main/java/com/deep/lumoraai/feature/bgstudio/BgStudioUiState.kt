package com.deep.lumoraai.feature.bgstudio

import android.graphics.Bitmap
import com.deep.lumoraai.feature.generation.GenerationAspectRatio

enum class BgStudioMode(val label: String) {
    Remove("Remove"),
    Replace("Replace"),
}

sealed interface BgStudioStatus {
    data object Idle : BgStudioStatus
    data object LoadingImage : BgStudioStatus
    data object Generating : BgStudioStatus
    data object Completed : BgStudioStatus
    data object TrialExpired : BgStudioStatus
    data class Error(val message: String) : BgStudioStatus
}

data class BgStudioUiState(
    val mode: BgStudioMode = BgStudioMode.Replace,
    val prompt: String = "",
    val negativePrompt: String = "",
    val aspectRatio: GenerationAspectRatio = GenerationAspectRatio.Portrait,
    val similarity: Float = 0.85f,
    val sourceBitmap: Bitmap? = null,
    val generatedPath: String? = null,
    val generatedPaths: List<String> = emptyList(),
    val generatedMimeType: String = "image/png",
    val generationProgress: Float? = null,
    val generationStatusText: String? = null,
    val status: BgStudioStatus = BgStudioStatus.Idle,
)
