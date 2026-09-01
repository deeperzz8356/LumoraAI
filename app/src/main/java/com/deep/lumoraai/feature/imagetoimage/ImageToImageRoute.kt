package com.deep.lumoraai.feature.imagetoimage

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.components.MediaViewerDialog

@Composable
fun ImageToImageRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: ImageToImageViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    ImageToImageScreen(
        uiState = uiState,
        onBack = onBack,
        onNavigate = onNavigate,
        onImageSelected = viewModel::loadImage,
        onPromptChanged = viewModel::updatePrompt,
        onStyleSelected = viewModel::selectStyle,
        onSimilarityChanged = viewModel::setSimilarity,
        onGenerationsChanged = viewModel::setGenerations,
        onGenerate = viewModel::generate,
        onDismissError = viewModel::dismissError,
    )

    if (uiState.generatedPath != null) {
        MediaViewerDialog(
            filePath = uiState.generatedPath,
            mediaType = "IMAGE",
            mimeType = uiState.generatedMimeType,
            title = "Image Ready",
            onDismiss = viewModel::clearResult,
        )
    }
}

