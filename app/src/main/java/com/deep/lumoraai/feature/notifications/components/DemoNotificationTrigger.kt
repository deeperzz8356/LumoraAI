package com.deep.lumoraai.feature.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.core.notification.NotificationDemo
import com.deep.lumoraai.core.notification.NotificationManager
import kotlinx.coroutines.CoroutineScope

/**
 * Demo notification trigger panel
 * Shows buttons to trigger various demo notifications for testing
 * Remove or hide this component in production builds
 */
@Composable
fun DemoNotificationTrigger(
    notificationManager: NotificationManager,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.BugReport,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(0f)
            )
            Text(
                text = "Demo Notification Triggers",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        // Buttons grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DemoButton(
                    label = "Job Complete",
                    onClick = {
                        NotificationDemo.triggerJobCompletion(notificationManager, scope)
                    },
                    modifier = Modifier.weight(1f)
                )
                DemoButton(
                    label = "Engagement",
                    onClick = {
                        NotificationDemo.triggerEngagementNotification(notificationManager, scope)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DemoButton(
                    label = "Feature",
                    onClick = {
                        NotificationDemo.triggerFeatureAnnouncement(notificationManager, scope)
                    },
                    modifier = Modifier.weight(1f)
                )
                DemoButton(
                    label = "Error",
                    onClick = {
                        NotificationDemo.triggerErrorNotification(notificationManager, scope)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DemoButton(
                    label = "Demo Batch",
                    onClick = {
                        NotificationDemo.triggerDemoBatch(notificationManager, scope)
                    },
                    modifier = Modifier.weight(1f)
                )
                DemoButton(
                    label = "Workflow",
                    onClick = {
                        NotificationDemo.simulateJobWorkflow(notificationManager, scope)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DemoButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
            contentColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
