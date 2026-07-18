package com.deep.lumoraai.feature.credits

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreditsRoute(
    onNext: () -> Unit,
    viewModel: CreditsViewModel = viewModel()
) {
    CreditsScreen(uiState = viewModel.uiState, viewModel = viewModel, onNext = onNext)
}