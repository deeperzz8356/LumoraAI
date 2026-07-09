package com.deep.lumoraai.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = SettingsUiState.Success(listOf("Settings fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Settings Light Preview", showBackground = true)
@Composable
fun SettingsLightPreview() {
    LumoraTheme(darkTheme = false) { SettingsScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Settings Dark Preview", showBackground = true)
@Composable
fun SettingsDarkPreview() {
    LumoraTheme(darkTheme = true) { SettingsScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Settings Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun SettingsTabletPreview() {
    LumoraTheme(darkTheme = true) { SettingsScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Settings Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun SettingsLandscapePreview() {
    LumoraTheme(darkTheme = true) { SettingsScreen(uiState = previewState, onNext = {}) }
}