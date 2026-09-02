package com.deep.lumoraai.feature.texttoimage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.feature.generation.GenerateNowButton
import com.deep.lumoraai.feature.generation.GeneratedMediaResult
import com.deep.lumoraai.feature.generation.GenerationControlsPanel
import com.deep.lumoraai.feature.generation.GenerationErrorText
import com.deep.lumoraai.feature.generation.GenerationScreenBg
import com.deep.lumoraai.feature.generation.GenerationTopBar
import com.deep.lumoraai.feature.generation.ImageStyleSection
import com.deep.lumoraai.feature.generation.PromptComposerCard
import com.deep.lumoraai.feature.imagetoimage.ImageStyle

@Composable
fun TextToImageScreen(
    uiState: TextToImageUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onPromptChanged: (String) -> Unit,
    onNegativePromptChanged: (String) -> Unit,
    onImprovePrompt: () -> Unit,
    onStyleSelected: (ImageStyle) -> Unit,
    onCreativityChanged: (Float) -> Unit,
    onGenerationsChanged: (Int) -> Unit,
    onGenerate: () -> Unit,
    onEditResult: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GenerationScreenBg)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GenerationTopBar(
                title = "Text 2 Image",
                onBack = onBack,
                onNotifications = { onNavigate(Screen.Notifications.route) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                PromptComposerCard(
                    prompt = uiState.prompt,
                    promptHint = "Describe the image you want to generate...",
                    negativePrompt = uiState.negativePrompt,
                    isImproving = uiState.isImprovingPrompt,
                    onPromptChanged = onPromptChanged,
                    onImprovePrompt = onImprovePrompt,
                    onNegativePromptChanged = onNegativePromptChanged
                )
                ImageStyleSection(selected = uiState.selectedStyle, onSelected = onStyleSelected)
                GenerationControlsPanel(
                    creativity = uiState.creativity,
                    onCreativityChanged = onCreativityChanged,
                    generations = uiState.generations,
                    onGenerationsChanged = onGenerationsChanged
                )
                GenerateNowButton(
                    isGenerating = uiState.isGenerating,
                    enabled = uiState.generatedPath == null,
                    onClick = onGenerate
                )
                GeneratedMediaResult(
                    filePath = uiState.generatedPath,
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
