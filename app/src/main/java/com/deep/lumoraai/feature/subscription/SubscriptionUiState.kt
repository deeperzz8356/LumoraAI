package com.deep.lumoraai.feature.subscription

import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan

sealed interface SubscriptionUiState {
    data object Loading : SubscriptionUiState
    data class Success(val plans: List<SubscriptionPlan>, val currentPlan: String? = null) : SubscriptionUiState
    data class Error(val message: String) : SubscriptionUiState
    data object Empty : SubscriptionUiState
}