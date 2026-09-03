package com.deep.lumoraai.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.notification.NotificationViewModel

@Composable
fun ProfileRoute(
    onNext: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    ProfileScreen(
        uiState = viewModel.uiState,
        onNext = onNext,
        onSignOut = {
            viewModel.signOut()
            onSignOut()
        },
        onDeleteAccount = {
            viewModel.deleteAccount(onDeleted = onDeleteAccount)
        },
        onNavigate = onNavigate,
        unreadCount = unreadCount
    )
}
