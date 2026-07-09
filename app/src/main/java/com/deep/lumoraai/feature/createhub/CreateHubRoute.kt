package com.deep.lumoraai.feature.createhub

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateHubRoute(
    onNext: () -> Unit,
    viewModel: CreateHubViewModel = viewModel()
) {
    CreateHubScreen(uiState = viewModel.uiState, onNext = onNext)
}