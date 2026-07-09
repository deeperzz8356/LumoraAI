package com.deep.lumoraai.feature.createhub

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = CreateHubUiState.Success(listOf("Create Hub fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Create Hub Light Preview", showBackground = true)
@Composable
fun CreateHubLightPreview() {
    LumoraTheme(darkTheme = false) { CreateHubScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Create Hub Dark Preview", showBackground = true)
@Composable
fun CreateHubDarkPreview() {
    LumoraTheme(darkTheme = true) { CreateHubScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Create Hub Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun CreateHubTabletPreview() {
    LumoraTheme(darkTheme = true) { CreateHubScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Create Hub Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun CreateHubLandscapePreview() {
    LumoraTheme(darkTheme = true) { CreateHubScreen(uiState = previewState, onNext = {}) }
}