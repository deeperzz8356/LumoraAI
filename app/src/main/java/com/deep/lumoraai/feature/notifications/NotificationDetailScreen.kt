package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addActionRow
import com.deep.lumoraai.core.nativeui.addPrimaryButton
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.domain.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationDetailScreen(
    notification: Notification,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LumoraXmlScreen(modifier = modifier) { binding ->
        binding.setupTopBar(
            titleText = "Notification Detail",
            subtitleText = notification.priority.name.lowercase().replaceFirstChar { it.uppercase() },
            onBack = onBackClick,
            actionIconRes = R.drawable.ic_lumora_delete,
            onAction = onDeleteClick
        )
        binding.resetContent()
        binding.content.addTextCard(notification.title, notification.message)
        binding.content.addSectionTitle("Details")
        binding.content.addTextCard("Received", notification.createdAt.label())
        binding.content.addTextCard("Type", notification.type.name.replace('_', ' '))
        binding.content.addTextCard("Status", if (notification.isRead) "Read" else "Unread")
        notification.imageUrl?.takeIf { it.isNotBlank() }?.let {
            binding.content.addActionRow("Image", it, R.drawable.ic_lumora_image)
        }
        if (notification.actionUrl != null) {
            binding.content.addPrimaryButton("OPEN") { onActionClick() }
        }
    }
}

private fun Long.label(): String =
    SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(this))
