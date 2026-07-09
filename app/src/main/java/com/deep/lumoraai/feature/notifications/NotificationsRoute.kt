package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NotificationsRoute(
    onNext: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    NotificationsScreen(uiState = viewModel.uiState, onNext = onNext)
}