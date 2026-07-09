package com.deep.lumoraai.feature.result

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ResultRoute(
    onNext: () -> Unit,
    viewModel: ResultViewModel = viewModel()
) {
    ResultScreen(uiState = viewModel.uiState, onNext = onNext)
}