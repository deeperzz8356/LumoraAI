package com.deep.lumoraai.feature.bgstudio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.navigation.Screen

@Composable
fun BgStudioRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    initialMode: String = "replace",
    viewModel: BgStudioViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    LaunchedEffect(initialMode) {
        // AI Replace mode hidden per UI change request — always resolve to Remove
        // regardless of the requested initialMode (Home/AITools may pass "replace").
        viewModel.selectMode(BgStudioMode.Remove)
    }

    LaunchedEffect(uiState.status) {
        when (uiState.status) {
            BgStudioStatus.TrialExpired -> {
                onNavigate(Screen.Auth.route)
                viewModel.resetStatus()
            }
            else -> Unit
        }
    }

    BgStudioScreen(
        uiState = uiState,
        onBack = onBack,
        onNavigate = onNavigate,
        onModeSelected = viewModel::selectMode,
        onPromptChanged = viewModel::updatePrompt,
        onNegativePromptChanged = viewModel::updateNegativePrompt,
        onAspectRatioChanged = viewModel::setAspectRatio,
        onSimilarityChanged = viewModel::setSimilarity,
        onImageSelected = viewModel::loadImage,
        onCreate = viewModel::create,
        // Keep the generated media visible in-place while editing.
        onEditResult = {},
        onDismissError = viewModel::resetStatus,
    )
}
