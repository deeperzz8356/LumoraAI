package com.deep.lumoraai.feature.credits

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = CreditsUiState.Success(listOf("Credits fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Credits Light Preview", showBackground = true)
@Composable
fun CreditsLightPreview() {
    LumoraTheme(darkTheme = false) { CreditsScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Credits Dark Preview", showBackground = true)
@Composable
fun CreditsDarkPreview() {
    LumoraTheme(darkTheme = true) { CreditsScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Credits Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun CreditsTabletPreview() {
    LumoraTheme(darkTheme = true) { CreditsScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Credits Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun CreditsLandscapePreview() {
    LumoraTheme(darkTheme = true) { CreditsScreen(uiState = previewState, onNext = {}) }
}