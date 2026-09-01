package com.deep.lumoraai.feature.texttovideo

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.components.MediaViewerDialog

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
        onStyleSelected = viewModel::selectStyle,
        onEngineSelected = viewModel::selectEngine,
        onMotionChanged = viewModel::setMotion,
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
