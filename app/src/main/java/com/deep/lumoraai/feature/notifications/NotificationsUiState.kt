package com.deep.lumoraai.feature.notifications

sealed interface NotificationsUiState {
    data object Loading : NotificationsUiState
    data class Success(val items: List<String>) : NotificationsUiState
    data class Error(val message: String) : NotificationsUiState
    data object Empty : NotificationsUiState
}