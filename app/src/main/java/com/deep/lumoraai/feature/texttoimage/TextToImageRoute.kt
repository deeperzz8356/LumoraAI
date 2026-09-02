package com.deep.lumoraai.feature.texttoimage

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TextToImageRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    initialPrompt: String? = null,
    viewModel: TextToImageViewModel = viewModel()
) {
    LaunchedEffect(initialPrompt) {
        viewModel.applyTemplatePrompt(initialPrompt)
    }

    val uiState = viewModel.uiState

    TextToImageScreen(
        uiState = uiState,
        onBack = onBack,
        onNavigate = onNavigate,
        onPromptChanged = viewModel::updatePrompt,
        onNegativePromptChanged = viewModel::updateNegativePrompt,
        onImprovePrompt = viewModel::improvePrompt,
        onStyleSelected = viewModel::selectStyle,
        onCreativityChanged = viewModel::setCreativity,
        onGenerationsChanged = viewModel::setGenerations,
        onGenerate = viewModel::generate,
        onEditResult = viewModel::clearResult,
        onDismissError = viewModel::dismissError,
    )
}
