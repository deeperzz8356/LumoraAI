package com.deep.lumoraai.feature.notifications

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.databinding.NotificationRowBinding
import com.deep.lumoraai.databinding.NotificationsScreenBinding
import com.deep.lumoraai.feature.notifications.model.NotificationModel
import com.deep.lumoraai.feature.notifications.model.NotificationType

private const val NotificationLime = 0xFFD6FF3F.toInt()
private const val NotificationMuted = 0xFF9AA5BB.toInt()

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
    LumoraXmlScreen(
        selectedTab = "notifications",
        onNavigate = onNavigate,
        modifier = modifier,
    ) { shell ->
        shell.topBar.visibility = View.GONE
        shell.content.removeAllViews()
        shell.content.setPadding(0, 0, 0, 0)
        val binding = NotificationsScreenBinding.inflate(LayoutInflater.from(shell.content.context), shell.content, true)

        binding.backButton.setOnClickListener { onBack() }
        binding.markReadButton.setOnClickListener { onMarkAllRead() }
        binding.settingsButton.setOnClickListener { onNavigate(Screen.Settings.route) }
        binding.restoreButton.setOnClickListener { onClearDismissed() }

        when (uiState) {
            NotificationsUiState.Loading -> binding.bindSummaryAndStatus(
                unread = 0,
                enabled = true,
                title = "Loading updates",
                subtitle = "Checking your studio inbox.",
                icon = R.drawable.ic_lumora_bell,
            )
            is NotificationsUiState.Error -> binding.bindSummaryAndStatus(
                unread = 0,
                enabled = true,
                title = "Unable to load",
                subtitle = uiState.message,
                icon = R.drawable.ic_lumora_close,
            )
            is NotificationsUiState.Empty -> binding.bindSummaryAndStatus(
                unread = 0,
                enabled = uiState.notificationsEnabled,
                title = "All caught up",
                subtitle = if (uiState.notificationsEnabled) {
                    "New generation and account updates will appear here."
                } else {
                    "Notifications are currently muted."
                },
                icon = R.drawable.ic_lumora_bell,
            )
            is NotificationsUiState.Success -> {
                binding.bindHeader(uiState.unreadCount, uiState.notificationsEnabled)
                if (uiState.items.isEmpty()) {
                    binding.bindStatus(
                        title = "All caught up",
                        subtitle = "New generation and account updates will appear here.",
                        icon = R.drawable.ic_lumora_bell,
                    )
                } else {
                    binding.statusCard.visibility = View.GONE
                    binding.listContainer.visibility = View.VISIBLE
                    binding.listContainer.removeAllViews()
                    binding.listContainer.addInboxTitle()
                    uiState.items.forEach { item ->
                        binding.listContainer.addView(
                            createNotificationRow(
                                parent = binding,
                                item = item,
                                onClick = { onNotificationClicked(item) },
                                onDismiss = { onDismissNotification(item.id) },
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun NotificationsScreenBinding.bindSummaryAndStatus(
    unread: Int,
    enabled: Boolean,
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
) {
    bindHeader(unread, enabled)
    listContainer.visibility = View.GONE
    listContainer.removeAllViews()
    bindStatus(title, subtitle, icon)
}

private fun NotificationsScreenBinding.bindHeader(unread: Int, enabled: Boolean) {
    liveLabel.text = if (enabled) "Inbox Live" else "Inbox Muted"
    unreadText.text = "$unread unread"
}

private fun NotificationsScreenBinding.bindStatus(title: String, subtitle: String, @DrawableRes icon: Int) {
    statusCard.visibility = View.VISIBLE
    statusIcon.setImageResource(icon)
    statusTitle.text = title
    statusSubtitle.text = subtitle
}

private fun android.widget.LinearLayout.addInboxTitle() {
    addView(TextView(context).apply {
        text = "Inbox"
        setTextColor(android.graphics.Color.WHITE)
        textSize = 25f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        includeFontPadding = false
    }, android.widget.LinearLayout.LayoutParams(
        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        topMargin = (40 * resources.displayMetrics.density).toInt()
    })
}

private fun createNotificationRow(
    parent: NotificationsScreenBinding,
    item: NotificationModel,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
): View {
    val binding = NotificationRowBinding.inflate(LayoutInflater.from(parent.root.context), parent.listContainer, false)
    binding.icon.setImageResource(item.type.iconRes)
    binding.title.text = item.title
    binding.message.text = item.message
    binding.time.text = item.timeLabel
    binding.root.alpha = if (item.isRead) 0.78f else 1f
    binding.root.setOnClickListener { onClick() }
    binding.dismissButton.setOnClickListener { onDismiss() }
    item.progress?.let {
        binding.progress.visibility = View.VISIBLE
        binding.progress.progress = (it.coerceIn(0f, 1f) * 100).toInt()
        binding.progress.progressTintList = ColorStateList.valueOf(NotificationLime)
        binding.progress.progressBackgroundTintList = ColorStateList.valueOf(NotificationMuted)
    } ?: run {
        binding.progress.visibility = View.GONE
    }
    return binding.root
}

@get:DrawableRes
private val NotificationType.iconRes: Int
    get() = when (this) {
        NotificationType.Generation -> R.drawable.ic_lumora_star
        NotificationType.Credits -> R.drawable.ic_lumora_check
        NotificationType.Account -> R.drawable.ic_lumora_settings
        NotificationType.System -> R.drawable.ic_lumora_bell
    }
