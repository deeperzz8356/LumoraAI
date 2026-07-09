package com.deep.lumoraai.feature.home

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeRoute(
    onNext: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    HomeScreen(uiState = viewModel.uiState, onNext = onNext)
}