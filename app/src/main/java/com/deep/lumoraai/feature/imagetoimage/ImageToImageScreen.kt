package com.deep.lumoraai.feature.imagetoimage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.generation.NativeGenerationConfig
import com.deep.lumoraai.feature.generation.NativeGenerationScreen
import com.deep.lumoraai.feature.generation.NativeGenerationSource
import com.deep.lumoraai.feature.generation.imageStyleItems

@Composable
fun ImageToImageScreen(
    uiState: ImageToImageUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onImagesSelected: (List<Uri>) -> Unit,
    onSourceRemoved: (String) -> Unit,
    onPromptChanged: (String) -> Unit,
    onNegativePromptChanged: (String) -> Unit,
    onAspectRatioChanged: (GenerationAspectRatio) -> Unit,
    onImprovePrompt: () -> Unit,
    onStyleSelected: (ImageStyle) -> Unit,
    onSimilarityChanged: (Float) -> Unit,
    onGenerationsChanged: (Int) -> Unit,
    onGenerate: () -> Unit,
    onEditResult: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ImageToImageBatch.MAX_SOURCE_IMAGES)
    ) { uris ->
        if (uris.isNotEmpty()) onImagesSelected(uris)
    }
    NativeGenerationScreen(
        config = NativeGenerationConfig(
            title = stringResource(com.deep.lumoraai.R.string.ui_image_2_image),
            promptHint = "Describe the image you want to generate...",
            promptOptional = true,
            showSingleUpload = false,
            singleUploadBitmap = null,
            onSingleUpload = null,
            multiSources = uiState.sourceImages.map { NativeGenerationSource(it.id, it.bitmap) },
            maxSources = ImageToImageBatch.MAX_SOURCE_IMAGES,
            isSourceBusy = uiState.isGenerating || uiState.isLoadingSources,
            onAddSources = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onRemoveSource = onSourceRemoved,
            prompt = uiState.prompt,
            negativePrompt = uiState.negativePrompt,
            isImprovingPrompt = uiState.isImprovingPrompt,
            selectedAspectRatio = uiState.aspectRatio,
            aspectRatioOptions = GenerationAspectRatio.entries,
            sliderLabel = stringResource(com.deep.lumoraai.R.string.ui_image_similarity),
            sliderValue = uiState.similarity,
            onSliderChanged = onSimilarityChanged,
            duration = null,
            onDurationChanged = null,
            styleItems = imageStyleItems(uiState.selectedStyle, onStyleSelected),
            isGenerating = uiState.isGenerating,
            generationProgress = uiState.generationProgress,
            generationStatusText = uiState.generationStatusText,
            generatedPath = uiState.generatedPath,
            generatedPaths = uiState.generatedPaths,
            generatedMimeType = uiState.generatedMimeType,
            mediaType = "IMAGE",
            generateEnabled = uiState.sourceImages.isNotEmpty() && !uiState.isLoadingSources,
            creditCost = GenerationGate.CREDITS_PER_IMAGE,
            bannerPlacement = AdPlacement.BANNER_IMG2IMG,
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
