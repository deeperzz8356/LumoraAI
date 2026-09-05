package com.deep.lumoraai.feature.credits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreditsRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: CreditsViewModel = viewModel()
) {
    // Bug 2 (isBugCondition2): drive the credits fetch from a single, stable
    // entry-scoped effect. LaunchedEffect(Unit) runs once when the screen enters
    // composition and does NOT re-fire on recomposition, so entering the screen
    // fetches exactly once. ensureLoaded() additionally coalesces/serves-fresh,
    // so any incidental extra trigger cannot amplify into multiple network calls.
    LaunchedEffect(Unit) {
        viewModel.ensureLoaded()
    }

    CreditsScreen(
        uiState = viewModel.uiState,
        viewModel = viewModel,
        onNext = onNext,
        onNavigate = onNavigate,
        onBack = onBack
    )
}
