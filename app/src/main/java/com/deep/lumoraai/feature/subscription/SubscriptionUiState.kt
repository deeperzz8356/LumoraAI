package com.deep.lumoraai.feature.subscription

import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan

/**
 * Legacy sealed UI state kept for previews/compat.
 * Runtime subscription flow uses [SubscriptionViewState].
 */
sealed interface SubscriptionUiState {
    data object Loading : SubscriptionUiState
    data class Success(val plans: List<SubscriptionPlan>, val currentPlan: String? = null) : SubscriptionUiState
    data class Error(val message: String) : SubscriptionUiState
    data object Empty : SubscriptionUiState
}
