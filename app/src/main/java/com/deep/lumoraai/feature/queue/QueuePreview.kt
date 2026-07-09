package com.deep.lumoraai.feature.queue

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = QueueUiState.Success(listOf("Queue fake item", "Preview data", "Compile-only screen"))

@Preview(name = "Queue Light Preview", showBackground = true)
@Composable
fun QueueLightPreview() {
    LumoraTheme(darkTheme = false) { QueueScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Queue Dark Preview", showBackground = true)
@Composable
fun QueueDarkPreview() {
    LumoraTheme(darkTheme = true) { QueueScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Queue Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun QueueTabletPreview() {
    LumoraTheme(darkTheme = true) { QueueScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Queue Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun QueueLandscapePreview() {
    LumoraTheme(darkTheme = true) { QueueScreen(uiState = previewState, onNext = {}) }
}