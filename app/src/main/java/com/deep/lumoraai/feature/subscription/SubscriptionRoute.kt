package com.deep.lumoraai.feature.subscription

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SubscriptionRoute(
    onNext: () -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    SubscriptionScreen(uiState = viewModel.uiState, onNext = onNext)
}