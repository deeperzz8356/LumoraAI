package com.deep.lumoraai.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OnboardingRoute(
    onNext: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    OnboardingScreen(uiState = viewModel.uiState, onNext = onNext)
}