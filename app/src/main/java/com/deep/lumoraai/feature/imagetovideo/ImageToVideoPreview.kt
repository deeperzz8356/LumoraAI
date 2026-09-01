package com.deep.lumoraai.feature.imagetovideo

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = ImageToVideoUiState()

@Composable
private fun PreviewContent() {
    ImageToVideoScreen(
        uiState = previewState,
        onBack = {},
        onNavigate = {},
        onImageSelected = {},
        onPromptChanged = {},
        onNegativePromptChanged = {},
        onImprovePrompt = {},
        onStyleSelected = {},
        onSimilarityChanged = {},
        onDurationChanged = {},
        onGenerationsChanged = {},
        onGenerate = {},
        onDismissError = {},
    )
}

@Preview(name = "Image To Video Light Preview", showBackground = true)
@Composable
fun ImageToVideoLightPreview() {
    LumoraTheme(darkTheme = false) { PreviewContent() }
}

@Preview(name = "Image To Video Dark Preview", showBackground = true)
@Composable
fun ImageToVideoDarkPreview() {
    LumoraTheme(darkTheme = true) { PreviewContent() }
}

@Preview(name = "Image To Video Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun ImageToVideoTabletPreview() {
    LumoraTheme(darkTheme = true) { PreviewContent() }
}

@Preview(name = "Image To Video Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun ImageToVideoLandscapePreview() {
    LumoraTheme(darkTheme = true) { PreviewContent() }
}
