package com.deep.lumoraai.feature.result

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = ResultUiState.Success(listOf("Result fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Result Light Preview", showBackground = true)
@Composable
fun ResultLightPreview() {
    LumoraTheme(darkTheme = false) { ResultScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Result Dark Preview", showBackground = true)
@Composable
fun ResultDarkPreview() {
    LumoraTheme(darkTheme = true) { ResultScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Result Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun ResultTabletPreview() {
    LumoraTheme(darkTheme = true) { ResultScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Result Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun ResultLandscapePreview() {
    LumoraTheme(darkTheme = true) { ResultScreen(uiState = previewState, onNext = {}) }
}