package com.deep.lumoraai.feature.subscription

sealed interface SubscriptionUiState {
    data object Loading : SubscriptionUiState
    data class Success(val items: List<String>) : SubscriptionUiState
    data class Error(val message: String) : SubscriptionUiState
    data object Empty : SubscriptionUiState
}