package com.deep.lumoraai.feature.imagetovideo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.generation.NativeGenerationConfig
import com.deep.lumoraai.feature.generation.NativeGenerationScreen
import com.deep.lumoraai.feature.generation.videoStyleItems
import com.deep.lumoraai.feature.imagetoimage.VideoStyle

@Composable
fun ImageToVideoScreen(
    uiState: ImageToVideoUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onPromptChanged: (String) -> Unit,
    onNegativePromptChanged: (String) -> Unit,
    onAspectRatioChanged: (GenerationAspectRatio) -> Unit,
    onImprovePrompt: () -> Unit,
    onStyleSelected: (VideoStyle) -> Unit,
    onSimilarityChanged: (Float) -> Unit,
    onDurationChanged: (Int) -> Unit,
    onGenerationsChanged: (Int) -> Unit,
    onGenerate: () -> Unit,
    onEditResult: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImageSelected(uri)
    }
    NativeGenerationScreen(
        config = NativeGenerationConfig(
            title = stringResource(com.deep.lumoraai.R.string.ui_image_2_video),
            promptHint = "Describe the video you want to generate...",
            promptOptional = true,
            showSingleUpload = true,
            singleUploadBitmap = uiState.sourceBitmap,
            onSingleUpload = { imagePicker.launch("image/*") },
            prompt = uiState.prompt,
            negativePrompt = uiState.negativePrompt,
            isImprovingPrompt = uiState.isImprovingPrompt,
            selectedAspectRatio = uiState.aspectRatio,
            aspectRatioOptions = GenerationAspectRatio.videoRatios,
            sliderLabel = stringResource(com.deep.lumoraai.R.string.ui_image_similarity),
            sliderValue = uiState.similarity,
            onSliderChanged = onSimilarityChanged,
            duration = uiState.duration,
            onDurationChanged = onDurationChanged,
            styleItems = videoStyleItems(uiState.selectedStyle, onStyleSelected),
            isGenerating = uiState.isGenerating,
            generationProgress = uiState.generationProgress,
            generationStatusText = uiState.generationStatusText,
            generatedPath = uiState.generatedPath,
            generatedPaths = uiState.generatedPaths,
            generatedMimeType = uiState.generatedMimeType,
            mediaType = "VIDEO",
            generateEnabled = uiState.sourceBitmap != null,
            creditCost = GenerationGate.CREDITS_PER_VIDEO,
            bannerPlacement = AdPlacement.BANNER_IMG2VIDEO,
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
