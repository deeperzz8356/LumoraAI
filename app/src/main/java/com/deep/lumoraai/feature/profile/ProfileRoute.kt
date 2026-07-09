package com.deep.lumoraai.feature.profile

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileRoute(
    onNext: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    ProfileScreen(
        uiState = viewModel.uiState,
        onNext = onNext,
        onSignOut = {
            viewModel.signOut()
            onSignOut()
        }
    )
}