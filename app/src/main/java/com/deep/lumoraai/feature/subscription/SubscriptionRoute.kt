package com.deep.lumoraai.feature.subscription

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.core.navigation.ResetLoadingOnLeave

@Composable
fun SubscriptionRoute(
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Push Remote Config values (subscription_enabled + plans JSON) into the
    // ViewModel as soon as the config store resolves.  The VM handles the
    // Disabled fast-path and plan overrides defensively — safe to call repeatedly.
    val adsConfig = LocalAdsConfigStore.current?.current

    if (adsConfig?.subscriptionEnabled != true) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val activeViewModel = viewModel ?: viewModel<SubscriptionViewModel>()
    LaunchedEffect(adsConfig?.subscriptionEnabled, adsConfig?.subscriptionPlansJson) {
        if (adsConfig != null) {
            activeViewModel.applyRemoteConfig(
                subscriptionEnabled = true,
                plansJson = adsConfig.subscriptionPlansJson,
            )
        }
    }

    // Reset any lingering purchase spinner when the user navigates away.
    ResetLoadingOnLeave { activeViewModel.clearPurchasingState() }

    SubscriptionScreen(
        uiState = activeViewModel.uiState,
        onSelectPlan = activeViewModel::selectPlan,
        onPurchase = {
            if (activity != null) activeViewModel.purchaseSelectedPlan(activity)
        },
        onRestore = activeViewModel::restorePurchases,
        onClearMessage = activeViewModel::clearPurchaseMessage,
        onBack = onBack,
        onNavigate = onNavigate,
    )
}
