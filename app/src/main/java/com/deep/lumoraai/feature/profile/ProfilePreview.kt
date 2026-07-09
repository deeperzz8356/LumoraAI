package com.deep.lumoraai.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = ProfileUiState.Success(listOf("Profile fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Profile Light Preview", showBackground = true)
@Composable
fun ProfileLightPreview() {
    LumoraTheme(darkTheme = false) { ProfileScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Profile Dark Preview", showBackground = true)
@Composable
fun ProfileDarkPreview() {
    LumoraTheme(darkTheme = true) { ProfileScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Profile Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun ProfileTabletPreview() {
    LumoraTheme(darkTheme = true) { ProfileScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Profile Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun ProfileLandscapePreview() {
    LumoraTheme(darkTheme = true) { ProfileScreen(uiState = previewState, onNext = {}) }
}