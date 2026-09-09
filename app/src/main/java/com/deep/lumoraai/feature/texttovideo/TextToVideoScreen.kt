package com.deep.lumoraai.feature.texttovideo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.deep.lumoraai.feature.generation.videoStyleItems
import com.deep.lumoraai.feature.generation.promoVideoStyleItems
import com.deep.lumoraai.feature.generation.GeneratedMediaLoading
import com.deep.lumoraai.feature.generation.GeneratedMediaResult
import com.deep.lumoraai.feature.generation.GenerationControlsPanel
import com.deep.lumoraai.feature.generation.GenerationErrorText
import com.deep.lumoraai.feature.generation.GenerationScreenBg
import com.deep.lumoraai.feature.generation.GenerationTopBar
import com.deep.lumoraai.feature.generation.PromptComposerCard
import com.deep.lumoraai.feature.generation.UploadImagePanel
import com.deep.lumoraai.feature.imagetoimage.VideoStyle
import kotlinx.coroutines.delay

@Composable
fun TextToVideoScreen(
    uiState: TextToVideoUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    isPromo: Boolean = false,
    onImageSelected: (Uri) -> Unit = {},
    onPromptChanged: (String) -> Unit,
    onNegativePromptChanged: (String) -> Unit,
    onAspectRatioChanged: (GenerationAspectRatio) -> Unit,
    onImprovePrompt: () -> Unit,
    onStyleSelected: (VideoStyle) -> Unit,
    onPromoStyleSelected: (com.deep.lumoraai.feature.imagetoimage.PromoVideoStyle) -> Unit = {},
    onMotionChanged: (Float) -> Unit,
    onDurationChanged: (Int) -> Unit,
    onGenerationsChanged: (Int) -> Unit,
    onGenerate: () -> Unit,
    onEditResult: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val showAdvancedSettings = remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImageSelected(uri)
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
                title = uiState.title,
                onBack = onBack,
                onNotifications = { onNavigate(Screen.Notifications.route) }
            )

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
                // Promo Video supports an optional uploaded source image.
                if (isPromo) {
                    UploadImagePanel(
                        bitmap = uiState.sourceBitmap,
                        isBusy = uiState.isGenerating,
                        onUpload = { imagePicker.launch("image/*") }
                    )
                }
                PromptComposerCard(
                    prompt = uiState.prompt,
                    promptHint = uiState.promptHint,
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
                        mediaType = "Video",
                        selectedAspectRatio = uiState.aspectRatio,
                        onAspectRatioSelected = onAspectRatioChanged,
                        negativePrompt = uiState.negativePrompt,
                        onNegativePromptChanged = onNegativePromptChanged,
                        motion = uiState.motion,
                        onMotionChanged = onMotionChanged,
                        duration = uiState.duration,
                        onDurationChanged = onDurationChanged,
                        generations = uiState.generations,
                        onGenerationsChanged = onGenerationsChanged
                    )
                }
                GeneratedMediaLoading(
                    isVisible = uiState.isGenerating,
                    mediaType = "VIDEO",
                    progress = uiState.generationProgress,
                    statusText = uiState.generationStatusText
                )
                GeneratedMediaResult(
                    filePath = uiState.generatedPath,
                    filePaths = uiState.generatedPaths,
                    mediaType = "VIDEO",
                    mimeType = uiState.generatedMimeType,
                    onEdit = onEditResult
                )
                GenerationErrorText(error = uiState.error, onDismissError = onDismissError)
            }
            }

            // Fixed bottom generate bar with collapsible Style / Ratio / Count.
            GenerationBottomBar(
                styleItems = if (isPromo) {
                    promoVideoStyleItems(uiState.selectedPromoStyle, onPromoStyleSelected)
                } else {
                    videoStyleItems(uiState.selectedStyle, onStyleSelected)
                },
                selectedAspectRatio = uiState.aspectRatio,
                onAspectRatioSelected = onAspectRatioChanged,
                aspectRatioOptions = GenerationAspectRatio.videoRatios,
                generations = uiState.generations,
                onGenerationsChanged = onGenerationsChanged,
                isGenerating = uiState.isGenerating,
                generateEnabled = uiState.prompt.isNotBlank(),
                creditCost = GenerationGate.CREDITS_PER_VIDEO * uiState.generations,
                onGenerate = onGenerate,
            )

            PlacementBanner(
                placement = if (isPromo) AdPlacement.BANNER_PROMO else AdPlacement.BANNER_TEXT2VIDEO,
                applyNavBarPadding = false,
            )
        }
    }
}
