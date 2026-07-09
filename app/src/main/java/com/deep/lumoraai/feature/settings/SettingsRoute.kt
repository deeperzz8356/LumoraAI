package com.deep.lumoraai.feature.settings

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsRoute(
    onNext: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    SettingsScreen(uiState = viewModel.uiState, onNext = onNext)
}