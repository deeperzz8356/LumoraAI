package com.deep.lumoraai.feature.history

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HistoryRoute(
    onNext: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    HistoryScreen(uiState = viewModel.uiState, onNext = onNext)
}