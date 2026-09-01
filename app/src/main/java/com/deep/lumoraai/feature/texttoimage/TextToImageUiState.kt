package com.deep.lumoraai.feature.texttoimage

import com.deep.lumoraai.feature.imagetoimage.ImageStyle

data class TextToImageUiState(
    val prompt: String = "",
    val negativePrompt: String = "",
    val selectedStyle: ImageStyle = ImageStyle.Photorealistic,
    val selectedModel: ImageModel = ImageModel.IMAGEN,
    val creativity: Float = 0.5f,
    val generations: Int = 2,
    val isGenerating: Boolean = false,
    val isImprovingPrompt: Boolean = false,
    val generatedPath: String? = null,
    val generatedMimeType: String = "image/png",
    val error: String? = null,
)

enum class ImageModel(val label: String) {
    IMAGEN("Imagen 4"),
    FLUX("Flux Pro"),
}
