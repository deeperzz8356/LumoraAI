package com.deep.lumoraai.feature.queue

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QueueRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: QueueViewModel = viewModel()
) {
    QueueScreen(uiState = viewModel.uiState, onNext = onNext, onNavigate = onNavigate)
}