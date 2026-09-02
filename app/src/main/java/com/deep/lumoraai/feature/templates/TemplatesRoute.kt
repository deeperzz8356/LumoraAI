package com.deep.lumoraai.feature.templates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.notification.NotificationViewModel

@Composable
fun TemplatesRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: TemplatesViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    TemplatesScreen(
        uiState = viewModel.uiState,
        onNext = onNext,
        onNavigate = onNavigate,
        unreadCount = unreadCount
    )
}
