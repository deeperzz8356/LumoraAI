package com.deep.lumoraai.feature.texttovideo

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
import com.deep.lumoraai.feature.generation.PromptComposerCard
import com.deep.lumoraai.feature.generation.VideoStyleSection
import com.deep.lumoraai.feature.imagetoimage.VideoStyle
import kotlinx.coroutines.delay

@Composable
fun TextToVideoScreen(
    uiState: TextToVideoUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onPromptChanged: (String) -> Unit,
    onNegativePromptChanged: (String) -> Unit,
    onAspectRatioChanged: (GenerationAspectRatio) -> Unit,
    onImprovePrompt: () -> Unit,
    onStyleSelected: (VideoStyle) -> Unit,
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

    LaunchedEffect(uiState.isGenerating, uiState.generatedPath) {
        if (uiState.isGenerating || uiState.generatedPath != null) {
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                PromptComposerCard(
                    prompt = uiState.prompt,
                    promptHint = uiState.promptHint,
                    negativePrompt = uiState.negativePrompt,
                    isImproving = uiState.isImprovingPrompt,
                    onPromptChanged = onPromptChanged,
                    onImprovePrompt = onImprovePrompt,
                    onNegativePromptChanged = onNegativePromptChanged,
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
                VideoStyleSection(selected = uiState.selectedStyle, onSelected = onStyleSelected)
                GenerateNowButton(
                    isGenerating = uiState.isGenerating,
                    enabled = uiState.generatedPath == null,
                    creditCost = GenerationGate.CREDITS_PER_VIDEO * uiState.generations,
                    onClick = onGenerate
                )
                GeneratedMediaLoading(
                    isVisible = uiState.isGenerating && uiState.generatedPath == null,
                    mediaType = "VIDEO"
                )
                GeneratedMediaResult(
                    filePath = uiState.generatedPath,
                    mediaType = "VIDEO",
                    mimeType = uiState.generatedMimeType,
                    onEdit = onEditResult
                )
                GenerationErrorText(error = uiState.error, onDismissError = onDismissError)
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
