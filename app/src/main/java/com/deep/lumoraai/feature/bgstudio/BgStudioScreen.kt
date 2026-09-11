package com.deep.lumoraai.feature.bgstudio

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.generation.NativeGenerationConfig
import com.deep.lumoraai.feature.generation.NativeGenerationScreen

@Composable
fun BgStudioScreen(
    uiState: BgStudioUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onModeSelected: (BgStudioMode) -> Unit,
    onPromptChanged: (String) -> Unit,
    onNegativePromptChanged: (String) -> Unit,
    onAspectRatioChanged: (GenerationAspectRatio) -> Unit,
    onSimilarityChanged: (Float) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onCreate: () -> Unit,
    onEditResult: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImageSelected(uri)
    }
    val error = (uiState.status as? BgStudioStatus.Error)?.message
    val isBusy = uiState.status == BgStudioStatus.LoadingImage || uiState.status == BgStudioStatus.Generating
    val statusText = when (uiState.status) {
        BgStudioStatus.LoadingImage -> "Loading image..."
        BgStudioStatus.Generating -> uiState.generationStatusText ?: "Background Studio is working..."
        else -> uiState.generationStatusText
    }

    NativeGenerationScreen(
        config = NativeGenerationConfig(
            title = "BG Studio",
            promptHint = "",
            promptOptional = uiState.mode == BgStudioMode.Remove,
            showPromptSection = false,
            showSingleUpload = true,
            singleUploadBitmap = uiState.sourceBitmap,
            onSingleUpload = { imagePicker.launch("image/*") },
            prompt = uiState.prompt,
            negativePrompt = uiState.negativePrompt,
            isImprovingPrompt = false,
            selectedAspectRatio = uiState.aspectRatio,
            aspectRatioOptions = GenerationAspectRatio.entries,
            sliderLabel = null,
            sliderValue = null,
            onSliderChanged = null,
            duration = null,
            onDurationChanged = null,
            styleItems = emptyList(),
            isGenerating = isBusy,
            generationProgress = uiState.generationProgress,
            generationStatusText = statusText,
            generatedPath = uiState.generatedPath,
            generatedPaths = uiState.generatedPaths,
            generatedMimeType = uiState.generatedMimeType,
            mediaType = "IMAGE",
            generateEnabled = uiState.sourceBitmap != null && !isBusy,
            creditCost = GenerationGate.CREDITS_PER_IMAGE,
            bannerPlacement = AdPlacement.BANNER_BG,
            showRatio = false,
            generateButtonText = "REMOVE BACKGROUND",
        ),
        onBack = onBack,
        onNavigate = onNavigate,
        onPromptChanged = onPromptChanged,
        onNegativePromptChanged = onNegativePromptChanged,
        onAspectRatioChanged = onAspectRatioChanged,
        onImprovePrompt = {},
        onGenerate = {
            onModeSelected(BgStudioMode.Remove)
            onCreate()
        },
        onEditResult = onEditResult,
        onDismissError = onDismissError,
        error = error,
        modifier = modifier,
    )
}
