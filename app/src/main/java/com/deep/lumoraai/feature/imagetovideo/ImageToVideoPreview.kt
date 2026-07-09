package com.deep.lumoraai.feature.imagetovideo

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = ImageToVideoUiState.Success(listOf("Image To Video fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Image To Video Light Preview", showBackground = true)
@Composable
fun ImageToVideoLightPreview() {
    LumoraTheme(darkTheme = false) { ImageToVideoScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Image To Video Dark Preview", showBackground = true)
@Composable
fun ImageToVideoDarkPreview() {
    LumoraTheme(darkTheme = true) { ImageToVideoScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Image To Video Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun ImageToVideoTabletPreview() {
    LumoraTheme(darkTheme = true) { ImageToVideoScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Image To Video Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun ImageToVideoLandscapePreview() {
    LumoraTheme(darkTheme = true) { ImageToVideoScreen(uiState = previewState, onNext = {}) }
}