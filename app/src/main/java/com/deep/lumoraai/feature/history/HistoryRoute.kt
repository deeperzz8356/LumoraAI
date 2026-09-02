package com.deep.lumoraai.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.notification.NotificationViewModel

@Composable
fun HistoryRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    viewModel: HistoryViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    HistoryScreen(
        uiState = viewModel.uiState,
        onNext = onNext,
        onNavigate = onNavigate,
        onDeleteItems = viewModel::deleteItems,
        unreadCount = unreadCount,
    )
}
