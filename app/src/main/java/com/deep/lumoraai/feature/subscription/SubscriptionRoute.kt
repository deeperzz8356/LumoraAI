package com.deep.lumoraai.feature.subscription

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SubscriptionRoute(
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    SubscriptionScreen(
        uiState = viewModel.uiState,
        onSelectPlan = viewModel::selectPlan,
        onPurchase = {
            if (activity != null) {
                viewModel.purchaseSelectedPlan(activity)
            }
        },
        onClearMessage = viewModel::clearPurchaseMessage,
        onBack = onBack,
        onNavigate = onNavigate
    )
}
