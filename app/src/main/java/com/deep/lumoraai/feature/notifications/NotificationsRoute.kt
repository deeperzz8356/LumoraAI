package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.deep.lumoraai.core.notification.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@Composable
fun NotificationsRoute(
    onNext: () -> Unit,
    onNavigate: ((String) -> Unit)? = null,
    viewModel: NotificationsViewModel = hiltViewModel(),
    notificationManager: NotificationManager? = null
) {
    val uiState = viewModel.uiState
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val scope = rememberCoroutineScope()

    NotificationsScreen(
        uiState = uiState,
        unreadCount = unreadCount,
        notifications = notifications,
        onNext = onNext,
        onMarkAsRead = { viewModel.markAsRead(it) },
        onDelete = { viewModel.deleteNotification(it) },
        onClearAll = { viewModel.clearAllNotifications() },
        onNotificationTap = { notification ->
            // Mark as read
            viewModel.markAsRead(notification.id)
            
            // Navigate if action URL is provided
            notification.actionUrl?.let { actionUrl ->
                // Parse deep link and navigate
                if (actionUrl.startsWith("com.deep.lumoraai://result")) {
                    // Extract resultId from deep link
                    val resultId = actionUrl.substringAfterLast("resultId=")
                    onNavigate?.invoke("com.deep.lumoraai.feature.result.ResultRoute?resultId=$resultId")
                } else {
                    // Generic deep link handling
                    onNavigate?.invoke(actionUrl)
                }
            }
        },
        notificationManager = notificationManager,
        scope = scope
    )
}