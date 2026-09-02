package com.deep.lumoraai.feature.notifications

import com.deep.lumoraai.feature.notifications.model.NotificationModel

sealed interface NotificationsUiState {
    data object Loading : NotificationsUiState
    data class Success(
        val items: List<NotificationModel>,
        val unreadCount: Int,
        val notificationsEnabled: Boolean,
    ) : NotificationsUiState
    data class Error(val message: String) : NotificationsUiState
    data class Empty(val notificationsEnabled: Boolean) : NotificationsUiState
}
