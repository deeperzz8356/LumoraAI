package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NotificationsRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    NotificationsScreen(
        uiState = viewModel.uiState,
        onBack = onBack,
        onNavigate = onNavigate,
        onMarkAllRead = viewModel::markAllRead,
        onNotificationClicked = { notification ->
            viewModel.markRead(notification.id)
            onNavigate(notification.route)
        },
        onDismissNotification = viewModel::dismiss,
        onClearDismissed = viewModel::clearDismissed
    )
}
