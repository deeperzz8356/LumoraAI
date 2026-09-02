package com.deep.lumoraai.feature.imagetoimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.GuestIdentity
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ImageToImageRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: ImageToImageViewModel = viewModel()
) {
    val context = LocalContext.current
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
