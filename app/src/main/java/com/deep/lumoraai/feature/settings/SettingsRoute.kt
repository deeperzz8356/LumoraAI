package com.deep.lumoraai.feature.settings

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    SettingsScreen(uiState = viewModel.uiState, viewModel = viewModel, onNext = onNext, onNavigate = onNavigate)
}