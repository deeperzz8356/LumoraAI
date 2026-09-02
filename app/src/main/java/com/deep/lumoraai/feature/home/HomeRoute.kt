package com.deep.lumoraai.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.notification.NotificationViewModel

@Composable
fun HomeRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    HomeScreen(
        uiState = homeViewModel.uiState,
        onNext = onNext,
        onNavigate = onNavigate,
        unreadCount = unreadCount,
        onNotificationClick = {
            onNavigate("com.deep.lumoraai.feature.notifications.NotificationsRoute")
        }
    )
}
