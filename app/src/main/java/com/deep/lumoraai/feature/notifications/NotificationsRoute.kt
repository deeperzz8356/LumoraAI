package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NotificationsRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    NotificationsScreen(
        uiState = uiState,
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
