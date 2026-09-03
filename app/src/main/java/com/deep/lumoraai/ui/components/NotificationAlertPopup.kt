package com.deep.lumoraai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource

/**
 * In-app notification alert that slides in from top
 */
@Composable
fun NotificationAlertPopup(
    title: String,
    message: String,
    type: AlertType = AlertType.INFO,
    duration: Long = 5000L,
    onDismiss: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isVisible = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(duration)
        isVisible.value = false
        onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible.value,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable { onClick?.invoke() },
            shape = RoundedCornerShape(8.dp),
            color = when (type) {
                AlertType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                AlertType.ERROR -> MaterialTheme.colorScheme.errorContainer
                AlertType.WARNING -> MaterialTheme.colorScheme.secondaryContainer
                AlertType.INFO -> MaterialTheme.colorScheme.tertiaryContainer
            },
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icon
                    Icon(
                        imageVector = when (type) {
                            AlertType.SUCCESS -> Icons.Default.CheckCircle
                            AlertType.ERROR -> Icons.Default.Error
                            AlertType.WARNING -> Icons.Default.Info
                            AlertType.INFO -> Icons.Default.Info
                        },
                        contentDescription = null,
                        modifier = Modifier.width(24.dp),
                        tint = when (type) {
                            AlertType.SUCCESS -> MaterialTheme.colorScheme.primary
                            AlertType.ERROR -> MaterialTheme.colorScheme.error
                            AlertType.WARNING -> MaterialTheme.colorScheme.secondary
                            AlertType.INFO -> MaterialTheme.colorScheme.tertiary
                        }
                    )

                    // Text
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                // Close button
                IconButton(
                    onClick = {
                        isVisible.value = false
                        onDismiss()
                    },
                    modifier = Modifier.width(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(com.deep.lumoraai.R.string.ui_close),
                        modifier = Modifier.width(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

enum class AlertType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}
