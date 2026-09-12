package com.deep.lumoraai.feature.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.PremiumActionRow
import com.deep.lumoraai.core.components.PremiumLime
import com.deep.lumoraai.core.components.PremiumMuted
import com.deep.lumoraai.core.components.PremiumPage
import com.deep.lumoraai.core.components.PremiumSection
import com.deep.lumoraai.core.components.PremiumStroke
import com.deep.lumoraai.core.components.PremiumSurface
import com.deep.lumoraai.core.components.PremiumSurfaceRaised
import com.deep.lumoraai.core.components.PremiumText
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.feature.notifications.model.NotificationModel
import com.deep.lumoraai.feature.notifications.model.NotificationType

@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClicked: (NotificationModel) -> Unit,
    onDismissNotification: (String) -> Unit,
    onClearDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PremiumPage(
        title = "Notifications",
        eyebrow = "Studio inbox",
        subtitle = "Updates from your creative workspace",
        onBack = onBack,
        modifier = modifier,
        action = {
            IconButton(onClick = { onNavigate(Screen.Settings.route) }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Settings, "Notification settings", tint = PremiumText)
            }
        },
    ) {
        when (uiState) {
            NotificationsUiState.Loading -> AppLoadingScreen()
            is NotificationsUiState.Error -> AppErrorScreen(uiState.message)
            is NotificationsUiState.Empty -> NotificationEmpty(uiState.notificationsEnabled, onClearDismissed)
            is NotificationsUiState.Success -> {
                NotificationSummary(uiState.unreadCount, uiState.notificationsEnabled)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PremiumActionRow("Queue", "Active jobs", Icons.Default.PlayCircle, { onNavigate(Screen.Queue.route) }, Modifier.weight(1f), trailing = "OPEN")
                    PremiumActionRow("History", "Finished work", Icons.Default.History, { onNavigate(Screen.History.route) }, Modifier.weight(1f), trailing = "VIEW")
                }
                PremiumSection("Inbox", "${uiState.items.size} updates in your studio") {
                    PremiumActionRow("Mark everything read", "Clear the unread state", Icons.Default.CheckCircle, onMarkAllRead, trailing = "DONE")
                    uiState.items.forEach { item ->
                        NotificationCard(item, { onNotificationClicked(item) }, { onDismissNotification(item.id) })
                    }
                    PremiumActionRow("Restore dismissed", "Bring hidden updates back", Icons.Default.Restore, onClearDismissed)
                }
            }
        }
    }
}

@Composable
private fun NotificationSummary(unreadCount: Int, enabled: Boolean) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = PremiumSurface, border = BorderStroke(1.dp, PremiumLime.copy(alpha = 0.24f))) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(52.dp).background(PremiumLime.copy(alpha = 0.12f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Notifications, null, tint = PremiumLime, modifier = Modifier.size(27.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("$unreadCount unread", color = PremiumText, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(if (enabled) "Your studio inbox is live" else "Notifications are muted", color = PremiumMuted, fontSize = 12.sp)
            }
            Text(if (enabled) "LIVE" else "MUTED", color = PremiumLime, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NotificationCard(item: NotificationModel, onClick: () -> Unit, onDismiss: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = if (item.isRead) PremiumSurface else PremiumSurfaceRaised, border = BorderStroke(1.dp, if (item.isRead) PremiumStroke else PremiumLime.copy(alpha = 0.28f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).background(PremiumLime.copy(alpha = 0.10f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(item.type.icon, null, tint = PremiumLime, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (!item.isRead) Box(Modifier.size(7.dp).background(PremiumLime, CircleShape))
                    Text(item.title, color = PremiumText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(item.message, color = PremiumMuted, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                item.progress?.let { LinearProgressIndicator(progress = { it }, modifier = Modifier.fillMaxWidth().height(3.dp), color = PremiumLime, trackColor = PremiumStroke) }
                Text(item.timeLabel, color = PremiumMuted.copy(alpha = 0.75f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Close, "Dismiss ${item.title}", tint = PremiumMuted, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
private fun NotificationEmpty(enabled: Boolean, onRestore: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = PremiumSurface, border = BorderStroke(1.dp, PremiumStroke)) {
        Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Inbox, null, tint = PremiumLime, modifier = Modifier.size(42.dp))
            Text("Inbox clear", color = PremiumText, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(if (enabled) "No updates yet." else "Notifications are currently muted.", color = PremiumMuted, fontSize = 13.sp)
            PremiumActionRow("Restore dismissed", "Bring hidden updates back", Icons.Default.Restore, onRestore)
        }
    }
}

private val NotificationType.icon: ImageVector
    get() = when (this) {
        NotificationType.Generation -> Icons.Default.AutoAwesome
        NotificationType.Credits -> Icons.Default.CreditCard
        NotificationType.Account -> Icons.Default.AccountCircle
        NotificationType.System -> Icons.Default.Notifications
    }
