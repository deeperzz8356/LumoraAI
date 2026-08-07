package com.deep.lumoraai.feature.subscription

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SubscriptionRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel(),
) {
    SubscriptionScreen(
        uiState = viewModel.uiState,
        onNext = onNext,
        onNavigate = onNavigate,
        onSelectPeriod = viewModel::selectBillingPeriod,
        onPurchase = viewModel::purchase,
        onRestore = viewModel::restorePurchases,
        onShowPaywall = viewModel::showPaywall,
        onDismissPaywall = viewModel::dismissPaywall,
        onShowCustomerCenter = viewModel::showCustomerCenter,
        onDismissCustomerCenter = viewModel::dismissCustomerCenter,
        onRefresh = viewModel::refresh,
    )
}
