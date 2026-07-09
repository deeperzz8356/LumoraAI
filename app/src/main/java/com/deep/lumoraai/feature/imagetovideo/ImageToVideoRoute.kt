package com.deep.lumoraai.feature.imagetovideo

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ImageToVideoRoute(
    onNext: () -> Unit,
    viewModel: ImageToVideoViewModel = viewModel()
) {
    ImageToVideoScreen(uiState = viewModel.uiState, onNext = onNext)
}