package com.deep.lumoraai.feature.texttoimage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.generation.NativeGenerationConfig
import com.deep.lumoraai.feature.generation.NativeGenerationScreen
import com.deep.lumoraai.feature.generation.imageStyleItems
import com.deep.lumoraai.feature.imagetoimage.ImageStyle

@Composable
fun TextToImageScreen(
    uiState: TextToImageUiState,
    mode: TextToImageMode = TextToImageMode.TextToImage,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onPromptChanged: (String) -> Unit,
    onNegativePromptChanged: (String) -> Unit,
    onAspectRatioChanged: (GenerationAspectRatio) -> Unit,
    onImprovePrompt: () -> Unit,
    onStyleSelected: (ImageStyle) -> Unit,
    onCreativityChanged: (Float) -> Unit,
    onGenerationsChanged: (Int) -> Unit,
    onGenerate: () -> Unit,
    onEditResult: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleRes = when (mode) {
        TextToImageMode.TextToImage -> com.deep.lumoraai.R.string.ui_text_2_image
        TextToImageMode.Logo -> com.deep.lumoraai.R.string.ui_logo
        TextToImageMode.Avatar -> com.deep.lumoraai.R.string.ui_ai_avatar
    }
    val promptHint = when (mode) {
        TextToImageMode.TextToImage -> "Describe the image you want to generate..."
        TextToImageMode.Logo -> "Describe the logo you want to generate..."
        TextToImageMode.Avatar -> "Describe the avatar you want to generate..."
    }
    NativeGenerationScreen(
        config = NativeGenerationConfig(
            title = stringResource(titleRes),
            promptHint = promptHint,
            promptOptional = false,
            showSingleUpload = false,
            singleUploadBitmap = null,
            onSingleUpload = null,
            prompt = uiState.prompt,
            negativePrompt = uiState.negativePrompt,
            isImprovingPrompt = uiState.isImprovingPrompt,
            selectedAspectRatio = uiState.aspectRatio,
            aspectRatioOptions = if (mode == TextToImageMode.Logo) listOf(GenerationAspectRatio.Square) else GenerationAspectRatio.entries,
            sliderLabel = stringResource(com.deep.lumoraai.R.string.ui_creativity),
            sliderValue = uiState.creativity,
            onSliderChanged = onCreativityChanged,
            duration = null,
            onDurationChanged = null,
            styleItems = if (mode == TextToImageMode.TextToImage) imageStyleItems(uiState.selectedStyle, onStyleSelected) else emptyList(),
            isGenerating = uiState.isGenerating,
            generationProgress = uiState.generationProgress,
            generationStatusText = uiState.generationStatusText,
            generatedPath = uiState.generatedPath,
            generatedPaths = uiState.generatedPaths,
            generatedMimeType = uiState.generatedMimeType,
            mediaType = "IMAGE",
            generateEnabled = uiState.prompt.isNotBlank(),
            creditCost = GenerationGate.CREDITS_PER_IMAGE,
            bannerPlacement = AdPlacement.BANNER_TEXT2IMG,
            showRatio = mode != TextToImageMode.Logo,
        ),
        onBack = onBack,
        onNavigate = onNavigate,
        onPromptChanged = onPromptChanged,
        onNegativePromptChanged = onNegativePromptChanged,
        onAspectRatioChanged = onAspectRatioChanged,
        onImprovePrompt = onImprovePrompt,
        onGenerate = onGenerate,
        onEditResult = onEditResult,
        onDismissError = onDismissError,
        error = uiState.error,
        modifier = modifier,
    )
}

enum class TextToImageMode {
    TextToImage,
    Logo,
    Avatar
}
