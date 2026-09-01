package com.deep.lumoraai.feature.photoenhance

import android.graphics.Bitmap

enum class EnhanceOption(val label: String) {
    Low("Low"),
    Med("Med"),
    High("High"),
    Ultra("Ultra"),
}

data class PhotoEnhanceUiState(
    val originalBitmap: Bitmap? = null,
    val enhancedBitmap: Bitmap? = null,
    val resolution: EnhanceOption = EnhanceOption.Med,
    val sharpness: Float = 0.5f,
    val lighting: EnhanceOption = EnhanceOption.Med,
    val isEnhancing: Boolean = false,
    val savedPath: String? = null,
    val error: String? = null,
)
