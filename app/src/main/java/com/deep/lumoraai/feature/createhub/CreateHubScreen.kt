package com.deep.lumoraai.feature.createhub

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.feature.createhub.model.VideoEngine
import com.deep.lumoraai.feature.generation.GenerateNowButton
import com.deep.lumoraai.feature.generation.GeneratedMediaLoading
import com.deep.lumoraai.feature.generation.GeneratedMediaResult
import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.generation.GenerationAspectRatioSection
import com.deep.lumoraai.feature.generation.GenerationControlsPanel
import com.deep.lumoraai.feature.generation.GenerationCountSection
import com.deep.lumoraai.feature.generation.GenerationErrorText
import com.deep.lumoraai.feature.generation.GenerationLime
import com.deep.lumoraai.feature.generation.GenerationMuted
import com.deep.lumoraai.feature.generation.GenerationPanel
import com.deep.lumoraai.feature.generation.GenerationScreenBg
import com.deep.lumoraai.feature.generation.GenerationTopBar
import com.deep.lumoraai.feature.generation.ImageStyleSection
import com.deep.lumoraai.feature.generation.PromptComposerCard
import com.deep.lumoraai.feature.generation.VideoStyleSection
import com.deep.lumoraai.feature.imagetoimage.ImageStyle
import com.deep.lumoraai.feature.imagetoimage.VideoStyle
import kotlinx.coroutines.delay

@Composable
fun CreateHubScreen(
    uiState: CreateHubUiState,
    initialPrompt: String? = null,
    initialTab: Int = 0,
    onGenerateImage: (String, String, Int, Int, String?, String?) -> Unit,
    onGenerateVideo: (String, String, String?, Int, String?, Int) -> Unit = { _, _, _, _, _, _ -> },
    onResetState: () -> Unit,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = onNext,
    unreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedMode by remember(initialTab) {
        mutableStateOf(if (initialTab == 1) CreateHubMode.Video else CreateHubMode.Image)
    }
    var prompt by remember(initialPrompt) { mutableStateOf(initialPrompt.orEmpty()) }
    var negativePrompt by remember { mutableStateOf("") }
    var imageStyle by remember { mutableStateOf(ImageStyle.Photorealistic) }
    var videoStyle by remember { mutableStateOf(VideoStyle.CinematicFilm) }
    var aspectRatio by remember { mutableStateOf(GenerationAspectRatio.Portrait) }
    var creativity by remember { mutableStateOf(0.5f) }
    var motion by remember { mutableStateOf(0.5f) }
    var duration by remember { mutableStateOf(5) }
    var generations by remember { mutableStateOf(1) }
    var showAdvancedSettings by remember { mutableStateOf(false) }

    val isGenerating = uiState is CreateHubUiState.Generating
    val generatedPath = when (uiState) {
        is CreateHubUiState.ImageGenerated -> uiState.filePath
        is CreateHubUiState.VideoGenerated -> uiState.filePath
        else -> null
    }
    val generatedMimeType = when (uiState) {
        is CreateHubUiState.ImageGenerated -> uiState.mimeType
        is CreateHubUiState.VideoGenerated -> uiState.mimeType
        else -> if (selectedMode == CreateHubMode.Video) "video/mp4" else "image/png"
    }
    val error = (uiState as? CreateHubUiState.Error)?.message

    LaunchedEffect(isGenerating, generatedPath) {
        if (isGenerating || generatedPath != null) {
            delay(160)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = GenerationScreenBg,
            bottomBar = { BottomNavigationBar(emptyList(), "createhub", onNavigate) }
        ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            GenerationTopBar(
                title = "Create Hub",
                onBack = onBack,
                onNotifications = { onNavigate(Screen.Notifications.route) },
                hasUnreadNotifications = unreadCount > 0,
                modifier = Modifier.statusBarsPadding()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                CreateHubModeTabs(
                    selectedMode = selectedMode,
                    onSelected = { mode ->
                        selectedMode = mode
                        aspectRatio = if (mode == CreateHubMode.Video) {
                            GenerationAspectRatio.Story
                        } else {
                            GenerationAspectRatio.Portrait
                        }
                    }
                )
                PromptComposerCard(
                    prompt = prompt,
                    promptHint = if (selectedMode == CreateHubMode.Video) {
                        "Describe the video you want to generate..."
                    } else {
                        "Describe the image you want to create..."
                    },
                    negativePrompt = negativePrompt,
                    isImproving = false,
                    onPromptChanged = { prompt = it.take(1000) },
                    onImprovePrompt = {},
                    onNegativePromptChanged = { negativePrompt = it.take(1000) },
                    isSettingsOpen = showAdvancedSettings,
                    onSettingsClick = { showAdvancedSettings = !showAdvancedSettings }
                )
                GenerationAspectRatioSection(
                    selected = aspectRatio,
                    onSelected = { aspectRatio = it }
                )
                GenerationCountSection(
                    generations = generations,
                    onGenerationsChanged = { generations = it.coerceIn(1, 4) }
                )
                if (showAdvancedSettings) {
                    GenerationControlsPanel(
                        mediaType = if (selectedMode == CreateHubMode.Video) "Video" else "Image",
                        selectedAspectRatio = aspectRatio,
                        onAspectRatioSelected = { aspectRatio = it },
                        negativePrompt = negativePrompt,
                        onNegativePromptChanged = { negativePrompt = it.take(1000) },
                        creativity = if (selectedMode == CreateHubMode.Image) creativity else null,
                        onCreativityChanged = if (selectedMode == CreateHubMode.Image) {
                            { creativity = it }
                        } else {
                            null
                        },
                        motion = if (selectedMode == CreateHubMode.Video) motion else null,
                        onMotionChanged = if (selectedMode == CreateHubMode.Video) {
                            { motion = it }
                        } else {
                            null
                        },
                        duration = if (selectedMode == CreateHubMode.Video) duration else null,
                        onDurationChanged = if (selectedMode == CreateHubMode.Video) {
                            { duration = it.coerceIn(5, 15) }
                        } else {
                            null
                        },
                        generations = generations,
                        onGenerationsChanged = { generations = it.coerceIn(1, 4) }
                    )
                }
                if (selectedMode == CreateHubMode.Video) {
                    VideoStyleSection(selected = videoStyle, onSelected = { videoStyle = it })
                } else {
                    ImageStyleSection(selected = imageStyle, onSelected = { imageStyle = it })
                }
                GenerateNowButton(
                    isGenerating = isGenerating,
                    enabled = generatedPath == null,
                    creditCost = if (selectedMode == CreateHubMode.Video) {
                        GenerationGate.CREDITS_PER_VIDEO * generations
                    } else {
                        GenerationGate.CREDITS_PER_IMAGE * generations
                    },
                    onClick = {
                        if (selectedMode == CreateHubMode.Video) {
                            onGenerateVideo(
                                prompt,
                                VideoEngine.FAST_DRAFT.modelId,
                                null,
                                (motion * 100).toInt().coerceIn(20, 90),
                                null,
                                duration
                            )
                        } else {
                            onGenerateImage(
                                prompt,
                                imageStyle.label,
                                aspectRatio.width,
                                aspectRatio.height,
                                negativePrompt.takeIf { it.isNotBlank() },
                                null
                            )
                        }
                    }
                )
                GeneratedMediaLoading(
                    isVisible = isGenerating && generatedPath == null,
                    mediaType = if (selectedMode == CreateHubMode.Video) "VIDEO" else "IMAGE"
                )
                GeneratedMediaResult(
                    filePath = generatedPath,
                    mediaType = if (selectedMode == CreateHubMode.Video) "VIDEO" else "IMAGE",
                    mimeType = generatedMimeType,
                    onEdit = onResetState
                )
                GenerationErrorText(error = error, onDismissError = onResetState)
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
        }
    }
}

private enum class CreateHubMode(val label: String) {
    Image("Image"),
    Video("Video")
}

@Composable
private fun CreateHubModeTabs(
    selectedMode: CreateHubMode,
    onSelected: (CreateHubMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GenerationPanel)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CreateHubMode.entries.forEach { mode ->
            val selected = mode == selectedMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) GenerationLime else Color.Transparent)
                    .clickable { onSelected(mode) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    color = if (selected) Color.Black else GenerationMuted,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
