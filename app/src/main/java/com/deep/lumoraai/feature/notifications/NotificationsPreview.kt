package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = NotificationsUiState.Success(listOf("Notifications fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Notifications Light Preview", showBackground = true)
@Composable
fun NotificationsLightPreview() {
    LumoraTheme(darkTheme = false) { NotificationsScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Notifications Dark Preview", showBackground = true)
@Composable
fun NotificationsDarkPreview() {
    LumoraTheme(darkTheme = true) { NotificationsScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Notifications Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun NotificationsTabletPreview() {
    LumoraTheme(darkTheme = true) { NotificationsScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Notifications Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun NotificationsLandscapePreview() {
    LumoraTheme(darkTheme = true) { NotificationsScreen(uiState = previewState, onNext = {}) }
}