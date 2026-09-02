package com.deep.lumoraai.feature.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.EmptyState
import com.deep.lumoraai.core.components.ErrorState
import com.deep.lumoraai.core.components.Loading
import com.deep.lumoraai.core.notification.NotificationManager
import com.deep.lumoraai.data.local.room.entity.NotificationEntity
import com.deep.lumoraai.feature.notifications.components.DemoNotificationTrigger
import com.deep.lumoraai.feature.notifications.components.NotificationCard
import kotlinx.coroutines.CoroutineScope

@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    unreadCount: Int = 0,
    notifications: List<NotificationEntity> = emptyList(),
    onNext: () -> Unit,
    onNotificationClick: (() -> Unit)? = null,
    onMarkAsRead: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onNotificationTap: (NotificationEntity) -> Unit = {},
    onClearAll: () -> Unit = {},
    notificationManager: NotificationManager? = null,
    scope: CoroutineScope? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppToolbar(
                title = "Notifications",
                unreadCount = unreadCount,
                onNotificationClick = onNotificationClick
            )
        },
        bottomBar = {
            BottomNavigationBar(
                items = listOf("home", "createhub", "queue", "profile"),
                selected = "notifications",
                onSelected = { onNext() }
            )
        }
    ) { padding ->
        when (uiState) {
            NotificationsUiState.Loading -> Loading(modifier = Modifier.padding(padding))
            NotificationsUiState.Empty -> EmptyState(
                title = "No Notifications",
                message = "You're all caught up! New notifications will appear here.",
                modifier = Modifier.padding(padding)
            )
            is NotificationsUiState.Error -> ErrorState(
                title = "Error",
                message = uiState.message,
                modifier = Modifier.padding(padding)
            )
            is NotificationsUiState.Success -> NotificationsContent(
                notifications = notifications,
                unreadCount = unreadCount,
                onMarkAsRead = onMarkAsRead,
                onDelete = onDelete,
                onNotificationTap = onNotificationTap,
                onClearAll = onClearAll,
                notificationManager = notificationManager,
                scope = scope,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun NotificationsContent(
    notifications: List<NotificationEntity>,
    unreadCount: Int,
    onMarkAsRead: (String) -> Unit,
    onDelete: (String) -> Unit,
    onNotificationTap: (NotificationEntity) -> Unit,
    onClearAll: () -> Unit,
    notificationManager: NotificationManager? = null,
    scope: CoroutineScope? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Demo trigger section (debug mode)
            if (notificationManager != null && scope != null) {
                item {
                    DemoNotificationTrigger(
                        notificationManager = notificationManager,
                        scope = scope
                    )
                }
            }

            // Header with action
            if (notifications.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (unreadCount > 0) {
                                Text(
                                    text = "$unreadCount unread",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(onClick = onClearAll) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Notifications list
            if (notifications.isNotEmpty()) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onMarkAsRead = onMarkAsRead,
                        onDelete = onDelete,
                        onTap = onNotificationTap
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                item {
                    EmptyState(
                        title = "No Notifications",
                        message = "You're all caught up! New notifications will appear here.",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}