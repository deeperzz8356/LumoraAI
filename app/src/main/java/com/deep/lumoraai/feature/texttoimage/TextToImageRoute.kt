package com.deep.lumoraai.feature.texttoimage

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TextToImageRoute(
    onNext: () -> Unit,
    viewModel: TextToImageViewModel = viewModel()
) {
    TextToImageScreen(uiState = viewModel.uiState, onNext = onNext)
}