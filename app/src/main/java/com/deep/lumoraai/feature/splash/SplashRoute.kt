package com.deep.lumoraai.feature.splash

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SplashRoute(
    onNext: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    SplashScreen(isReady = viewModel.isReady, onNext = onNext)
}