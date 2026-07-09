package com.deep.lumoraai.feature.subscription

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = SubscriptionUiState.Success(listOf("Subscription fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Subscription Light Preview", showBackground = true)
@Composable
fun SubscriptionLightPreview() {
    LumoraTheme(darkTheme = false) { SubscriptionScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Subscription Dark Preview", showBackground = true)
@Composable
fun SubscriptionDarkPreview() {
    LumoraTheme(darkTheme = true) { SubscriptionScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Subscription Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun SubscriptionTabletPreview() {
    LumoraTheme(darkTheme = true) { SubscriptionScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Subscription Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun SubscriptionLandscapePreview() {
    LumoraTheme(darkTheme = true) { SubscriptionScreen(uiState = previewState, onNext = {}) }
}