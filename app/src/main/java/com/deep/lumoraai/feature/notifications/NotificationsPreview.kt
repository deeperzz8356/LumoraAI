package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.theme.LumoraTheme
import com.deep.lumoraai.feature.notifications.model.NotificationModel
import com.deep.lumoraai.feature.notifications.model.NotificationType

private val previewState = NotificationsUiState.Success(
    items = listOf(
        NotificationModel(
            id = "preview-job",
            title = "Text to video is rendering",
            message = "Composing motion and camera movement - 62%",
            timeLabel = "In progress",
            type = NotificationType.Generation,
            route = Screen.Queue.route,
            progress = 0.62f
        ),
        NotificationModel(
            id = "preview-credits",
            title = "Credits balance updated",
            message = "You have 160 LUM credits available.",
            timeLabel = "Now",
            type = NotificationType.Credits,
            route = Screen.Credits.route,
            isRead = true
        )
    ),
    unreadCount = 1,
    notificationsEnabled = true
)

@Composable
private fun PreviewContent() {
    NotificationsScreen(
        uiState = previewState,
        onBack = {},
        onNavigate = {},
        onMarkAllRead = {},
        onNotificationClicked = {},
        onDismissNotification = {},
        onClearDismissed = {}
    )
}

@Preview(name = "Notifications Light Preview", showBackground = true)
@Composable
fun NotificationsLightPreview() {
    LumoraTheme(darkTheme = false) { PreviewContent() }
}

@Preview(name = "Notifications Dark Preview", showBackground = true)
@Composable
fun NotificationsDarkPreview() {
    LumoraTheme(darkTheme = true) { PreviewContent() }
}

@Preview(name = "Notifications Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun NotificationsTabletPreview() {
    LumoraTheme(darkTheme = true) { PreviewContent() }
}

@Preview(name = "Notifications Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun NotificationsLandscapePreview() {
    LumoraTheme(darkTheme = true) { PreviewContent() }
}
