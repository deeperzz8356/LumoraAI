package com.deep.lumoraai.feature.texttovideo

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.GuestIdentity
import com.google.firebase.auth.FirebaseAuth

@Composable
fun TextToVideoRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    isPromo: Boolean = false,
    initialPrompt: String? = null,
    viewModel: TextToVideoViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(isPromo, initialPrompt) {
        viewModel.configure(isPromo = isPromo, initialPrompt = initialPrompt)
    }

    val uiState = viewModel.uiState
    LaunchedEffect(uiState.error) {
        if (
            uiState.error == GenerationGate.insufficientCreditsMessage() &&
            FirebaseAuth.getInstance().currentUser?.isAnonymous == true
        ) {
            GuestIdentity.markTrialExhausted(context)
            viewModel.dismissError()
            onNavigate(Screen.Auth.route)
        }
    }

    TextToVideoScreen(
        uiState = uiState,
        onBack = onBack,
        onNavigate = onNavigate,
        onPromptChanged = viewModel::updatePrompt,
        onNegativePromptChanged = viewModel::updateNegativePrompt,
        onAspectRatioChanged = viewModel::setAspectRatio,
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
