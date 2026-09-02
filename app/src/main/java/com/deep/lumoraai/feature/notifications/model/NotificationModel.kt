package com.deep.lumoraai.feature.notifications.model

data class NotificationModel(
    val id: String,
    val title: String,
    val message: String,
    val timeLabel: String,
    val type: NotificationType,
    val route: String,
    val isRead: Boolean = false,
    val progress: Float? = null,
)

enum class NotificationType {
    Generation,
    Credits,
    Account,
    System,
}
