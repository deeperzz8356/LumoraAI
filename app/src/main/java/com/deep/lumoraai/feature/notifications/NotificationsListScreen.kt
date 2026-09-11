package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addActionRow
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.domain.model.Notification
import com.deep.lumoraai.domain.model.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsListScreen(
    onNotificationClick: (String) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LumoraXmlScreen(modifier = modifier) { binding ->
        binding.setupTopBar(
            titleText = "Notifications",
            subtitleText = "${uiState.unreadCount} unread",
            onBack = null,
            actionIconRes = R.drawable.ic_lumora_check,
            onAction = viewModel::markAllAsRead
        )
        binding.resetContent()
        if (uiState.isLoading) {
            binding.content.addTextCard("Loading", "Checking notifications.")
            return@LumoraXmlScreen
        }
        uiState.error?.let { binding.content.addTextCard("Error", it) }
        binding.content.addSectionTitle("Filters")
        binding.content.addActionRow("All", "Show every notification", R.drawable.ic_lumora_bell) { viewModel.filterByType(null) }
        binding.content.addActionRow("Task Completion", "Finished generation alerts", R.drawable.ic_lumora_check) {
            viewModel.filterByType(NotificationType.TASK_COMPLETION)
        }
        binding.content.addActionRow("Engagement", "Usage reminders", R.drawable.ic_lumora_info) {
            viewModel.filterByType(NotificationType.ENGAGEMENT)
        }
        binding.content.addActionRow("Feature Announcements", "New product updates", R.drawable.ic_lumora_star) {
            viewModel.filterByType(NotificationType.FEATURE_ANNOUNCEMENT)
        }
        binding.content.addSectionTitle("Inbox")
        if (uiState.notifications.isEmpty()) {
            binding.content.addTextCard("No notifications", "You are all caught up.")
        } else {
            uiState.notifications.forEach { item ->
                binding.content.addActionRow(
                    title = if (item.isRead) item.title else "NEW  ${item.title}",
                    subtitle = "${item.message}  |  ${item.createdAt.label()}  |  ${item.type.name.replace('_', ' ')}",
                    icon = item.iconRes,
                    onClick = {
                        viewModel.markAsRead(item.id)
                        onNotificationClick(item.id)
                    }
                )
            }
        }
        binding.content.addActionRow("Clear All", "Delete all notifications", R.drawable.ic_lumora_delete) {
            viewModel.clearAllNotifications()
        }
    }
}

private val Notification.iconRes: Int
    get() = when (type) {
        NotificationType.TASK_COMPLETION -> R.drawable.ic_lumora_check
        NotificationType.ENGAGEMENT -> R.drawable.ic_lumora_info
        NotificationType.FEATURE_ANNOUNCEMENT -> R.drawable.ic_lumora_star
    }

private fun Long.label(): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(this))
