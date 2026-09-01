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
        viewModel.selectMode(
            if (initialMode.equals("remove", ignoreCase = true)) {
                BgStudioMode.Remove
            } else {
                BgStudioMode.Replace
            }
        )
    }

    LaunchedEffect(uiState.status) {
        when (uiState.status) {
            BgStudioStatus.Generating -> onNavigate(Screen.Queue.route)
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
        onImageSelected = viewModel::loadImage,
        onCreate = viewModel::create,
        onDismissError = viewModel::resetStatus,
    )
}
