package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addActionRow
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
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
    modifier: Modifier = Modifier
) {
    LumoraXmlScreen(
        selectedTab = "notifications",
        onNavigate = onNavigate,
        modifier = modifier
    ) { binding ->
        binding.setupTopBar(
            titleText = "Notifications",
            subtitleText = "Updates from your studio",
            onBack = onBack,
            actionIconRes = R.drawable.ic_lumora_settings,
            onAction = { onNavigate(Screen.Settings.route) }
        )
        binding.resetContent()
        when (uiState) {
            NotificationsUiState.Loading -> binding.content.addTextCard("Loading", "Checking your inbox.")
            is NotificationsUiState.Error -> binding.content.addTextCard("Error", uiState.message)
            is NotificationsUiState.Empty -> {
                binding.content.addTextCard(
                    "Inbox clear",
                    if (uiState.notificationsEnabled) "No updates yet." else "Notifications are currently muted."
                )
                binding.content.addActionRow("Restore dismissed", "Bring back dismissed notifications", R.drawable.ic_lumora_history, onClearDismissed)
            }
            is NotificationsUiState.Success -> {
                binding.content.addTextCard(
                    title = "${uiState.unreadCount} unread",
                    subtitle = if (uiState.notificationsEnabled) "Inbox live" else "Notifications muted"
                )
                binding.content.addActionRow("Mark as read", "Mark every visible notification as read", R.drawable.ic_lumora_check, onMarkAllRead)
                binding.content.addActionRow("Queue", "See active generation jobs", R.drawable.ic_lumora_video) {
                    onNavigate(Screen.Queue.route)
                }
                binding.content.addActionRow("History", "Open finished creations", R.drawable.ic_lumora_history) {
                    onNavigate(Screen.History.route)
                }
                binding.content.addSectionTitle("Inbox")
                binding.content.addActionRow("Restore dismissed", "Bring back dismissed notifications", R.drawable.ic_lumora_history, onClearDismissed)
                uiState.items.forEach { item ->
                    binding.content.addActionRow(
                        title = if (item.isRead) item.title else "NEW  ${item.title}",
                        subtitle = listOf(item.message, item.timeLabel, item.progress?.let { "${(it * 100).toInt()}%" })
                            .filterNotNull()
                            .filter { it.isNotBlank() }
                            .joinToString("  |  "),
                        icon = item.type.iconRes,
                        onClick = { onNotificationClicked(item) }
                    ).chevron.setOnClickListener {
                        onDismissNotification(item.id)
                    }
                }
            }
        }
    }
}

private val NotificationType.iconRes: Int
    get() = when (this) {
        NotificationType.Generation -> R.drawable.ic_lumora_play
        NotificationType.Credits -> R.drawable.ic_lumora_star
        NotificationType.Account -> R.drawable.ic_lumora_avatar
        NotificationType.System -> R.drawable.ic_lumora_bell
    }
