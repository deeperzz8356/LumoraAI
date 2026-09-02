package com.deep.lumoraai.feature.subscription

import com.deep.lumoraai.data.billing.BillingState
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan

sealed interface SubscriptionUiState {
    data object Loading : SubscriptionUiState
    data class Success(
        val plans: List<SubscriptionPlan>,
        val selectedPlanId: String,
        val isDeveloperMode: Boolean,
        val isPurchasing: Boolean = false,
        val purchaseMessage: String? = null,
        val billingState: BillingState = BillingState.Disconnected,
        val restoredProductIds: List<String> = emptyList()
    ) : SubscriptionUiState
    data class Error(val message: String) : SubscriptionUiState
}
