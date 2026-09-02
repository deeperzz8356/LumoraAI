package com.deep.lumoraai.feature.texttoimage

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.GuestIdentity
import com.google.firebase.auth.FirebaseAuth

@Composable
fun TextToImageRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    initialPrompt: String? = null,
    viewModel: TextToImageViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(initialPrompt) {
        viewModel.applyTemplatePrompt(initialPrompt)
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
