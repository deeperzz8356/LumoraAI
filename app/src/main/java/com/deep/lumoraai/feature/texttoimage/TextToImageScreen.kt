package com.deep.lumoraai.feature.texttoimage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.PlacementBanner
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.feature.generation.GenerationBottomBar
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.generation.imageStyleItems
import com.deep.lumoraai.feature.generation.GeneratedMediaLoading
import com.deep.lumoraai.feature.generation.GeneratedMediaResult
import com.deep.lumoraai.feature.generation.GenerationControlsPanel
import com.deep.lumoraai.feature.generation.GenerationErrorText
import com.deep.lumoraai.feature.generation.GenerationScreenBg
import com.deep.lumoraai.feature.generation.GenerationTopBar
import com.deep.lumoraai.feature.generation.PromptComposerCard
import com.deep.lumoraai.feature.imagetoimage.ImageStyle
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource

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
    val scrollState = rememberScrollState()
    val showAdvancedSettings = remember { mutableStateOf(false) }
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

    LaunchedEffect(uiState.isGenerating, uiState.generatedPaths) {
        if (uiState.isGenerating || uiState.generatedPaths.isNotEmpty()) {
            delay(160)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GenerationScreenBg)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GenerationTopBar(
                title = stringResource(titleRes),
                onBack = onBack,
                onNotifications = { onNavigate(Screen.Notifications.route) }
            )

            // Content scrolls behind the pinned Generate button. The button is
            // overlaid at the bottom of this Box (like the banner), and the
            // scroll content reserves bottom space so nothing hides under it.
            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .imePadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 18.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    PromptComposerCard(
                        prompt = uiState.prompt,
                        promptHint = promptHint,
                        negativePrompt = uiState.negativePrompt,
                        isImproving = uiState.isImprovingPrompt,
                        onPromptChanged = onPromptChanged,
                        onImprovePrompt = onImprovePrompt,
                        onNegativePromptChanged = onNegativePromptChanged,
                        isSettingsOpen = showAdvancedSettings.value,
                        onSettingsClick = { showAdvancedSettings.value = !showAdvancedSettings.value },
                        showSettingsAction = false
                    )
                    if (showAdvancedSettings.value) {
                        GenerationControlsPanel(
                            mediaType = "Image",
                            selectedAspectRatio = uiState.aspectRatio,
                            onAspectRatioSelected = onAspectRatioChanged,
                            negativePrompt = uiState.negativePrompt,
                            onNegativePromptChanged = onNegativePromptChanged,
                            creativity = uiState.creativity,
                            onCreativityChanged = onCreativityChanged,
                            generations = uiState.generations,
                            onGenerationsChanged = onGenerationsChanged
                        )
                    }
                    GeneratedMediaLoading(
                        isVisible = uiState.isGenerating,
                        mediaType = "IMAGE",
                        progress = uiState.generationProgress,
                        statusText = uiState.generationStatusText
                    )
                    GeneratedMediaResult(
                        filePath = uiState.generatedPath,
                        filePaths = uiState.generatedPaths,
                        mediaType = "IMAGE",
                        mimeType = uiState.generatedMimeType,
                        onEdit = onEditResult
                    )
                    GenerationErrorText(error = uiState.error, onDismissError = onDismissError)
                }
            }

            // Fixed bottom generate bar with collapsible Style / Ratio / Count.
            GenerationBottomBar(
                styleItems = if (mode == TextToImageMode.TextToImage) {
                    imageStyleItems(uiState.selectedStyle, onStyleSelected)
                } else {
                    emptyList()
                },
                selectedAspectRatio = uiState.aspectRatio,
                onAspectRatioSelected = onAspectRatioChanged,
                aspectRatioOptions = if (mode == TextToImageMode.Logo) {
                    listOf(GenerationAspectRatio.Square)
                } else {
                    GenerationAspectRatio.entries
                },
                generations = uiState.generations,
                onGenerationsChanged = onGenerationsChanged,
                isGenerating = uiState.isGenerating,
                generateEnabled = uiState.prompt.isNotBlank(),
                creditCost = GenerationGate.CREDITS_PER_IMAGE * uiState.generations,
                onGenerate = onGenerate,
                showRatio = mode != TextToImageMode.Logo,
            )

            PlacementBanner(placement = AdPlacement.BANNER_TEXT2IMG, applyNavBarPadding = false)
        }
    }
}

enum class TextToImageMode {
    TextToImage,
    Logo,
    Avatar
}
