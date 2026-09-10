package com.deep.lumoraai.feature.texttovideo

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
import com.deep.lumoraai.feature.generation.promoVideoStyleItems
import com.deep.lumoraai.feature.generation.videoStyleItems
import com.deep.lumoraai.feature.imagetoimage.PromoVideoStyle
import com.deep.lumoraai.feature.imagetoimage.VideoStyle

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
    onPromoStyleSelected: (PromoVideoStyle) -> Unit = {},
    onMotionChanged: (Float) -> Unit,
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
            title = uiState.title,
            promptHint = uiState.promptHint,
            promptOptional = false,
            showSingleUpload = isPromo,
            singleUploadBitmap = uiState.sourceBitmap,
            onSingleUpload = { imagePicker.launch("image/*") },
            prompt = uiState.prompt,
            negativePrompt = uiState.negativePrompt,
            isImprovingPrompt = uiState.isImprovingPrompt,
            selectedAspectRatio = uiState.aspectRatio,
            aspectRatioOptions = GenerationAspectRatio.videoRatios,
            sliderLabel = "Motion",
            sliderValue = uiState.motion,
            onSliderChanged = onMotionChanged,
            duration = uiState.duration,
            onDurationChanged = onDurationChanged,
            styleItems = if (isPromo) {
                promoVideoStyleItems(uiState.selectedPromoStyle, onPromoStyleSelected)
            } else {
                videoStyleItems(uiState.selectedStyle, onStyleSelected)
            },
            isGenerating = uiState.isGenerating,
            generationProgress = uiState.generationProgress,
            generationStatusText = uiState.generationStatusText,
            generatedPath = uiState.generatedPath,
            generatedPaths = uiState.generatedPaths,
            generatedMimeType = uiState.generatedMimeType,
            mediaType = "VIDEO",
            generateEnabled = uiState.prompt.isNotBlank(),
            creditCost = GenerationGate.CREDITS_PER_VIDEO,
            bannerPlacement = if (isPromo) AdPlacement.BANNER_PROMO else AdPlacement.BANNER_TEXT2VIDEO,
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
