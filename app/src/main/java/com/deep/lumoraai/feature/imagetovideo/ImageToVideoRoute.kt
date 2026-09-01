package com.deep.lumoraai.feature.imagetovideo

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.components.MediaViewerDialog

@Composable
fun ImageToVideoRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: ImageToVideoViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    ImageToVideoScreen(
        uiState = uiState,
        onBack = onBack,
        onNavigate = onNavigate,
        onImageSelected = viewModel::loadImage,
        onPromptChanged = viewModel::updatePrompt,
        onNegativePromptChanged = viewModel::updateNegativePrompt,
        onImprovePrompt = viewModel::improvePrompt,
        onStyleSelected = viewModel::selectStyle,
        onSimilarityChanged = viewModel::setSimilarity,
        onDurationChanged = viewModel::setDuration,
        onGenerationsChanged = viewModel::setGenerations,
        onGenerate = viewModel::generate,
        onDismissError = viewModel::dismissError,
    )

    if (uiState.generatedPath != null) {
        MediaViewerDialog(
            filePath = uiState.generatedPath,
            mediaType = "VIDEO",
            mimeType = uiState.generatedMimeType,
            title = "Video Ready",
            onDismiss = viewModel::clearResult,
        )
    }
}
