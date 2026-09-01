package com.deep.lumoraai.feature.photoenhance

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PhotoEnhanceRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: PhotoEnhanceViewModel = viewModel()
) {
    PhotoEnhanceScreen(
        uiState = viewModel.uiState,
        onBack = onBack,
        onNavigate = onNavigate,
        onImageSelected = viewModel::loadImage,
        onResolutionSelected = viewModel::setResolution,
        onSharpnessChanged = viewModel::setSharpness,
        onLightingSelected = viewModel::setLighting,
        onEnhance = viewModel::enhance,
    )
}
