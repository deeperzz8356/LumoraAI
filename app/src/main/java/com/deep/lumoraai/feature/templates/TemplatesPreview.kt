package com.deep.lumoraai.feature.templates

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = TemplatesUiState.Success(
    imageTemplates = realImageTemplates,
    videoTemplates = realVideoTemplates,
)

@Preview(name = "Templates Light Preview", showBackground = true)
@Composable
fun TemplatesLightPreview() {
    LumoraTheme(darkTheme = false) { TemplatesScreen(uiState = previewState, onNext = {}, onNavigate = {}) }
}

@Preview(name = "Templates Dark Preview", showBackground = true)
@Composable
fun TemplatesDarkPreview() {
    LumoraTheme(darkTheme = true) { TemplatesScreen(uiState = previewState, onNext = {}, onNavigate = {}) }
}

@Preview(name = "Templates Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun TemplatesTabletPreview() {
    LumoraTheme(darkTheme = true) { TemplatesScreen(uiState = previewState, onNext = {}, onNavigate = {}) }
}

@Preview(name = "Templates Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun TemplatesLandscapePreview() {
    LumoraTheme(darkTheme = true) { TemplatesScreen(uiState = previewState, onNext = {}, onNavigate = {}) }
}
