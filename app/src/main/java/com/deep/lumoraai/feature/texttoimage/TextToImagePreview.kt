package com.deep.lumoraai.feature.texttoimage

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = TextToImageUiState.Success(listOf("Text To Image fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Text To Image Light Preview", showBackground = true)
@Composable
fun TextToImageLightPreview() {
    LumoraTheme(darkTheme = false) { TextToImageScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Text To Image Dark Preview", showBackground = true)
@Composable
fun TextToImageDarkPreview() {
    LumoraTheme(darkTheme = true) { TextToImageScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Text To Image Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun TextToImageTabletPreview() {
    LumoraTheme(darkTheme = true) { TextToImageScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Text To Image Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun TextToImageLandscapePreview() {
    LumoraTheme(darkTheme = true) { TextToImageScreen(uiState = previewState, onNext = {}) }
}