package com.deep.lumoraai.feature.subscription

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SubscriptionRoute(
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    SubscriptionScreen(
        uiState = viewModel.uiState,
        onSelectPlan = viewModel::selectPlan,
        onPurchase = viewModel::purchaseSelectedPlan,
        onClearMessage = viewModel::clearPurchaseMessage,
        onBack = onBack,
        onNavigate = onNavigate
    )
}
