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
    mode: TextToImageMode = TextToImageMode.TextToImage,
    viewModel: TextToImageViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(initialPrompt) {
        viewModel.applyTemplatePrompt(initialPrompt)
    }
    LaunchedEffect(mode) {
        if (mode == TextToImageMode.Logo) {
            viewModel.setAspectRatio(com.deep.lumoraai.feature.generation.GenerationAspectRatio.Square)
        }
        if (mode != TextToImageMode.TextToImage) {
            viewModel.selectStyle(com.deep.lumoraai.feature.imagetoimage.ImageStyle.NoStyle)
        }
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
        mode = mode,
        onBack = onBack,
        onNavigate = onNavigate,
        onPromptChanged = viewModel::updatePrompt,
        onNegativePromptChanged = viewModel::updateNegativePrompt,
        onAspectRatioChanged = viewModel::setAspectRatio,
        onImprovePrompt = { viewModel.improvePrompt(mode) },
        onStyleSelected = viewModel::selectStyle,
        onCreativityChanged = viewModel::setCreativity,
        onGenerationsChanged = viewModel::setGenerations,
        onGenerate = { viewModel.generate(mode) },
        // Editing must keep the generated media visible in-place. The preview is
        // cleared naturally when the user changes the prompt, so tapping Edit
        // should only return focus to the editor, not wipe the result.
        onEditResult = {},
        onDismissError = viewModel::dismissError,
    )
}
