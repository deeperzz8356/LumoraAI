package com.deep.lumoraai.feature.texttovideo

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TextToVideoRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    isPromo: Boolean = false,
    initialPrompt: String? = null,
    viewModel: TextToVideoViewModel = viewModel()
) {
    LaunchedEffect(isPromo, initialPrompt) {
        viewModel.configure(isPromo = isPromo, initialPrompt = initialPrompt)
    }

    val uiState = viewModel.uiState

    TextToVideoScreen(
        uiState = uiState,
        onBack = onBack,
        onNavigate = onNavigate,
        onPromptChanged = viewModel::updatePrompt,
        onNegativePromptChanged = viewModel::updateNegativePrompt,
        onImprovePrompt = viewModel::improvePrompt,
        onStyleSelected = viewModel::selectStyle,
        onMotionChanged = viewModel::setMotion,
        onDurationChanged = viewModel::setDuration,
        onGenerationsChanged = viewModel::setGenerations,
        onGenerate = viewModel::generate,
        onEditResult = viewModel::clearResult,
        onDismissError = viewModel::dismissError,
    )
}
