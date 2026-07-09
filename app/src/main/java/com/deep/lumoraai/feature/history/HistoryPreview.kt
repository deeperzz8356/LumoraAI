package com.deep.lumoraai.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = HistoryUiState.Success(listOf("History fake item", "Preview data", "Compile-only screen"))

@Preview(name = "History Light Preview", showBackground = true)
@Composable
fun HistoryLightPreview() {
    LumoraTheme(darkTheme = false) { HistoryScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "History Dark Preview", showBackground = true)
@Composable
fun HistoryDarkPreview() {
    LumoraTheme(darkTheme = true) { HistoryScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "History Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun HistoryTabletPreview() {
    LumoraTheme(darkTheme = true) { HistoryScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "History Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun HistoryLandscapePreview() {
    LumoraTheme(darkTheme = true) { HistoryScreen(uiState = previewState, onNext = {}) }
}