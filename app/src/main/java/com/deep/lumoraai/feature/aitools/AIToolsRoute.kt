package com.deep.lumoraai.feature.aitools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.notification.NotificationViewModel

@Composable
fun AIToolsRoute(
    onNavigate: (String) -> Unit = {},
    viewModel: AIToolsViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
) {
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    AIToolsScreen(
        credits = viewModel.credits,
        onNavigate = onNavigate,
        unreadCount = unreadCount,
    )
}
