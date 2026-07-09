package com.deep.lumoraai.feature.texttovideo

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = TextToVideoUiState.Success(listOf("Text To Video fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Text To Video Light Preview", showBackground = true)
@Composable
fun TextToVideoLightPreview() {
    LumoraTheme(darkTheme = false) { TextToVideoScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Text To Video Dark Preview", showBackground = true)
@Composable
fun TextToVideoDarkPreview() {
    LumoraTheme(darkTheme = true) { TextToVideoScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Text To Video Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun TextToVideoTabletPreview() {
    LumoraTheme(darkTheme = true) { TextToVideoScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Text To Video Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun TextToVideoLandscapePreview() {
    LumoraTheme(darkTheme = true) { TextToVideoScreen(uiState = previewState, onNext = {}) }
}