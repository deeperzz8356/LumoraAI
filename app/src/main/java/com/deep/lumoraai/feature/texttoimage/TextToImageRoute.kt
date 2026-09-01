package com.deep.lumoraai.feature.texttoimage

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.components.MediaViewerDialog

@Composable
fun TextToImageRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: TextToImageViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    TextToImageScreen(
        uiState = uiState,
        onBack = onBack,
        onNavigate = onNavigate,
        onPromptChanged = viewModel::updatePrompt,
        onStyleSelected = viewModel::selectStyle,
        onModelSelected = viewModel::selectModel,
        onCreativityChanged = viewModel::setCreativity,
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
