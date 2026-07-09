package com.deep.lumoraai.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = HomeUiState.Success(listOf("Home fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Home Light Preview", showBackground = true)
@Composable
fun HomeLightPreview() {
    LumoraTheme(darkTheme = false) { HomeScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Home Dark Preview", showBackground = true)
@Composable
fun HomeDarkPreview() {
    LumoraTheme(darkTheme = true) { HomeScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Home Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun HomeTabletPreview() {
    LumoraTheme(darkTheme = true) { HomeScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Home Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun HomeLandscapePreview() {
    LumoraTheme(darkTheme = true) { HomeScreen(uiState = previewState, onNext = {}) }
}