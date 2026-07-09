package com.deep.lumoraai.feature.texttovideo

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TextToVideoRoute(
    onNext: () -> Unit,
    viewModel: TextToVideoViewModel = viewModel()
) {
    TextToVideoScreen(uiState = viewModel.uiState, onNext = onNext)
}