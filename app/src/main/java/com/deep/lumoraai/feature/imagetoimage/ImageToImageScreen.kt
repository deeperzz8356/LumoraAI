package com.deep.lumoraai.feature.imagetoimage

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
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.feature.generation.GenerateNowButton
import com.deep.lumoraai.feature.generation.GenerationAspectRatioSection
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.generation.GenerationCountSection
import com.deep.lumoraai.feature.generation.GeneratedMediaLoading
import com.deep.lumoraai.feature.generation.GeneratedMediaResult
import com.deep.lumoraai.feature.generation.GenerationControlsPanel
import com.deep.lumoraai.feature.generation.GenerationErrorText
import com.deep.lumoraai.feature.generation.GenerationScreenBg
import com.deep.lumoraai.feature.generation.GenerationTopBar
import com.deep.lumoraai.feature.generation.ImageStyleSection
import com.deep.lumoraai.feature.generation.PromptComposerCard
import com.deep.lumoraai.feature.generation.UploadImagePanel
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource

@Composable
fun ImageToImageScreen(
    uiState: ImageToImageUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
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
                title = stringResource(com.deep.lumoraai.R.string.ui_image_2_image),
                onBack = onBack,
                onNotifications = { onNavigate(Screen.Notifications.route) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                UploadImagePanel(
                    bitmap = uiState.sourceBitmap,
                    isBusy = uiState.isGenerating,
                    onUpload = { imagePicker.launch("image/*") }
                )
                PromptComposerCard(
                    prompt = uiState.prompt,
                    promptHint = "Describe the image you want to generate...",
                    negativePrompt = uiState.negativePrompt,
                    isImproving = uiState.isImprovingPrompt,
                    onPromptChanged = onPromptChanged,
                    onImprovePrompt = onImprovePrompt,
                    onNegativePromptChanged = onNegativePromptChanged,
                    onUpload = { imagePicker.launch("image/*") },
                    isSettingsOpen = showAdvancedSettings.value,
                    onSettingsClick = { showAdvancedSettings.value = !showAdvancedSettings.value }
                )
                GenerationAspectRatioSection(
                    selected = uiState.aspectRatio,
                    onSelected = onAspectRatioChanged
                )
                GenerationCountSection(
                    generations = uiState.generations,
                    onGenerationsChanged = onGenerationsChanged
                )
                if (showAdvancedSettings.value) {
                    GenerationControlsPanel(
                        mediaType = "Image",
                        selectedAspectRatio = uiState.aspectRatio,
                        onAspectRatioSelected = onAspectRatioChanged,
                        negativePrompt = uiState.negativePrompt,
                        onNegativePromptChanged = onNegativePromptChanged,
                        similarity = uiState.similarity,
                        onSimilarityChanged = onSimilarityChanged,
                        generations = uiState.generations,
                        onGenerationsChanged = onGenerationsChanged
                    )
                }
                ImageStyleSection(selected = uiState.selectedStyle, onSelected = onStyleSelected)
                GenerateNowButton(
                    isGenerating = uiState.isGenerating,
                    enabled = uiState.prompt.isNotBlank() && uiState.sourceBitmap != null,
                    creditCost = GenerationGate.CREDITS_PER_IMAGE * uiState.generations,
                    onClick = onGenerate
                )
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
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
