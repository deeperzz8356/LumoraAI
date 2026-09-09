package com.deep.lumoraai.feature.imagetoimage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.R
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
import com.deep.lumoraai.feature.generation.CollapsiblePromptComposerCard
import com.deep.lumoraai.feature.generation.GenerationTopBar
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource

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
    val scrollState = rememberScrollState()
    val showAdvancedSettings = remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ImageToImageBatch.MAX_SOURCE_IMAGES)
    ) { uris ->
        if (uris.isNotEmpty()) onImagesSelected(uris)
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
                onNotifications = { onNavigate(Screen.Notifications.route) },
                onCredits = { onNavigate(Screen.Credits.route) }
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
                SourceImagesPanel(
                    sources = uiState.sourceImages,
                    isBusy = uiState.isGenerating || uiState.isLoadingSources,
                    onAddImages = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onRemoveSource = onSourceRemoved,
                )
                // Prompt is optional here (generation works from the uploaded
                // image alone), so it's a collapsible dropdown, collapsed by
                // default.
                CollapsiblePromptComposerCard(
                    prompt = uiState.prompt,
                    promptHint = "Describe the image you want to generate...",
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
                        similarity = uiState.similarity,
                        onSimilarityChanged = onSimilarityChanged,
                        generations = 1,
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
                styleItems = imageStyleItems(uiState.selectedStyle, onStyleSelected),
                selectedAspectRatio = uiState.aspectRatio,
                onAspectRatioSelected = onAspectRatioChanged,
                aspectRatioOptions = GenerationAspectRatio.entries,
                generations = 1,
                onGenerationsChanged = onGenerationsChanged,
                isGenerating = uiState.isGenerating,
                // Prompt is optional: only require at least one source image.
                generateEnabled = uiState.sourceImages.isNotEmpty() && !uiState.isLoadingSources,
                creditCost = GenerationGate.CREDITS_PER_IMAGE,
                onGenerate = onGenerate,
            )

            PlacementBanner(placement = AdPlacement.BANNER_IMG2IMG, applyNavBarPadding = false)
        }
    }
}

@Composable
private fun SourceImagesPanel(
    sources: List<ImageToImageSource>,
    isBusy: Boolean,
    onAddImages: () -> Unit,
    onRemoveSource: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.ui_source_image), color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                "${sources.size} / ${ImageToImageBatch.MAX_SOURCE_IMAGES}",
                color = Color(0xFFD6FF2F),
                fontWeight = FontWeight.Bold,
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sources, key = { it.id }) { source ->
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = source.bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.ui_source_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    IconButton(
                        enabled = !isBusy,
                        onClick = { onRemoveSource(source.id) },
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ui_close), tint = Color.White)
                    }
                }
            }
            if (sources.size < ImageToImageBatch.MAX_SOURCE_IMAGES) {
                item {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color(0xFFD6FF2F).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .then(if (isBusy) Modifier else Modifier.clickable(onClick = onAddImages)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = stringResource(R.string.ui_upload_image), tint = Color(0xFFD6FF2F))
                            Spacer(Modifier.height(4.dp))
                            Text(stringResource(R.string.ui_upload_image), color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        if (sources.isEmpty()) {
            Text(stringResource(R.string.ui_tap_to_select_file), color = Color(0xFF94A0B8))
        }
    }
}
