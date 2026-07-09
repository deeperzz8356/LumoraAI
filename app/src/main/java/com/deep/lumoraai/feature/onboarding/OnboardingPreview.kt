package com.deep.lumoraai.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = OnboardingUiState.Success(listOf("Onboarding fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Onboarding Light Preview", showBackground = true)
@Composable
fun OnboardingLightPreview() {
    LumoraTheme(darkTheme = false) { OnboardingScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Onboarding Dark Preview", showBackground = true)
@Composable
fun OnboardingDarkPreview() {
    LumoraTheme(darkTheme = true) { OnboardingScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Onboarding Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun OnboardingTabletPreview() {
    LumoraTheme(darkTheme = true) { OnboardingScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Onboarding Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun OnboardingLandscapePreview() {
    LumoraTheme(darkTheme = true) { OnboardingScreen(uiState = previewState, onNext = {}) }
}