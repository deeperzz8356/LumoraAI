package com.deep.lumoraai.feature.imagetoimage

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

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
        onNegativePromptChanged = viewModel::updateNegativePrompt,
        onImprovePrompt = viewModel::improvePrompt,
        onStyleSelected = viewModel::selectStyle,
        onSimilarityChanged = viewModel::setSimilarity,
        onGenerationsChanged = viewModel::setGenerations,
        onGenerate = viewModel::generate,
        onEditResult = viewModel::clearResult,
        onDismissError = viewModel::dismissError,
    )
}
