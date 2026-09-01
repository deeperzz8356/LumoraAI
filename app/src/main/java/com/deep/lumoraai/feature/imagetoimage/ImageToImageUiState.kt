package com.deep.lumoraai.feature.imagetoimage

import android.graphics.Bitmap

data class ImageToImageUiState(
    val sourceBitmap: Bitmap? = null,
    val prompt: String = "",
    val selectedStyle: ImageStyle = ImageStyle.All,
    val similarity: Float = 0.5f,
    val generations: Int = 2,
    val isGenerating: Boolean = false,
    val generatedPath: String? = null,
    val generatedMimeType: String = "image/png",
    val error: String? = null,
)

enum class ImageStyle(val label: String) {
    All("All"),
    MyStyle("My Style"),
    Animal("Animal"),
    Portrait("Portrait"),
    Concept("Concept"),
}

