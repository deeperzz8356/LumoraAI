package com.deep.lumoraai.feature.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.deep.lumoraai.domain.model.NotificationType
import com.deep.lumoraai.ui.components.NotificationCard
import androidx.compose.ui.res.stringResource

/**
 * Main notifications list screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsListScreen(
    onNotificationClick: (String) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilter by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.unreadCount > 0) {
                                Text(
                                    text = "${uiState.unreadCount} unread",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showFilter = !showFilter }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(com.deep.lumoraai.R.string.ui_filter)
                        )
                    }
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = stringResource(com.deep.lumoraai.R.string.ui_mark_all_as_read)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading && uiState.notifications.isEmpty()) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.notifications.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier
                            .height(64.dp)
                            .width(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No notifications",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You're all caught up!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            // Notifications list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Filter chips
                if (showFilter) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "Filter by type",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = uiState.filterType == null,
                                    onClick = { viewModel.filterByType(null) },
                                    label = { Text(stringResource(com.deep.lumoraai.R.string.ui_all)) }
                                )
                                FilterChip(
                                    selected = uiState.filterType == NotificationType.TASK_COMPLETION,
                                    onClick = { viewModel.filterByType(NotificationType.TASK_COMPLETION) },
                                    label = { Text(stringResource(com.deep.lumoraai.R.string.ui_tasks)) }
                                )
                                FilterChip(
                                    selected = uiState.filterType == NotificationType.ENGAGEMENT,
                                    onClick = { viewModel.filterByType(NotificationType.ENGAGEMENT) },
                                    label = { Text(stringResource(com.deep.lumoraai.R.string.ui_engagement)) }
                                )
                                FilterChip(
                                    selected = uiState.filterType == NotificationType.FEATURE_ANNOUNCEMENT,
                                    onClick = { viewModel.filterByType(NotificationType.FEATURE_ANNOUNCEMENT) },
                                    label = { Text(stringResource(com.deep.lumoraai.R.string.ui_features)) }
                                )
                            }
                        }
                    }
                }

                // Notification items
                items(uiState.notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onNotificationClick = { onNotificationClick(it.id) },
                        onMarkAsRead = { viewModel.markAsRead(it) },
                        onDelete = { viewModel.deleteNotification(it) }
                    )
                }

                // Footer with clear all button
                item {
                    if (uiState.notifications.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Button(
                                onClick = { viewModel.clearAllNotifications() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(20.dp)
                                        .width(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(com.deep.lumoraai.R.string.ui_clear_all_notifications))
                            }
                        }
                    }
                }
            }
        }

        // Error state
        uiState.error?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
