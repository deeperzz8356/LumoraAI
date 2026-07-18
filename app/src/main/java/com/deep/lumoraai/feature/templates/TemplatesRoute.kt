package com.deep.lumoraai.feature.templates

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TemplatesRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: TemplatesViewModel = viewModel()
) {
    TemplatesScreen(uiState = viewModel.uiState, onNext = onNext, onNavigate = onNavigate)
}