package com.deep.lumoraai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deep.lumoraai.domain.model.Notification
import com.deep.lumoraai.domain.model.NotificationPriority
import com.deep.lumoraai.domain.model.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.stringResource

/**
 * Single notification card component
 */
@Composable
fun NotificationCard(
    notification: Notification,
    onNotificationClick: (Notification) -> Unit,
    onMarkAsRead: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (notification.priority) {
        NotificationPriority.HIGH -> MaterialTheme.colorScheme.errorContainer
        NotificationPriority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer
        NotificationPriority.LOW -> MaterialTheme.colorScheme.surfaceContainer
    }

    val borderColor = when (notification.priority) {
        NotificationPriority.HIGH -> MaterialTheme.colorScheme.error
        NotificationPriority.MEDIUM -> MaterialTheme.colorScheme.primary
        NotificationPriority.LOW -> MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onNotificationClick(notification) },
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left side: Icon + Content
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Priority icon
                Icon(
                    imageVector = when (notification.type) {
                        NotificationType.TASK_COMPLETION -> Icons.Default.DoneAll
                        NotificationType.ENGAGEMENT -> Icons.Default.Info
                        NotificationType.FEATURE_ANNOUNCEMENT -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when (notification.priority) {
                        NotificationPriority.HIGH -> MaterialTheme.colorScheme.error
                        NotificationPriority.MEDIUM -> MaterialTheme.colorScheme.primary
                        NotificationPriority.LOW -> MaterialTheme.colorScheme.outline
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    // Title
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Message
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Timestamp
                    Text(
                        text = formatTimestamp(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Right side: Image + Actions
            Column(
                modifier = Modifier.width(60.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Image (if available)
                if (notification.imageUrl != null) {
                    AsyncImage(
                        model = notification.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Unread indicator
                if (!notification.isRead) {
                    Surface(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50.dp)),
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                }

                // Delete button
                IconButton(
                    onClick = { onDelete(notification.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(com.deep.lumoraai.R.string.ui_delete),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Format timestamp for display
 */
private fun formatTimestamp(createdAt: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - createdAt

    return when {
        diffMillis < 60 * 1000 -> "Just now"
        diffMillis < 60 * 60 * 1000 -> "${diffMillis / (60 * 1000)} min ago"
        diffMillis < 24 * 60 * 60 * 1000 -> "${diffMillis / (60 * 60 * 1000)} hours ago"
        diffMillis < 7 * 24 * 60 * 60 * 1000 -> "${diffMillis / (24 * 60 * 60 * 1000)} days ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(createdAt))
    }
}
