package com.deep.lumoraai.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    // Refresh the language subtitle whenever the screen resumes (e.g. after
    // returning from the language picker). The ViewModel can outlive an Activity
    // recreate(), so its init snapshot may be stale; re-reading on resume keeps
    // the displayed language in sync with the applied locale immediately.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshLanguage()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    SettingsScreen(
        uiState = viewModel.uiState,
        onNavigate = onNavigate,
        onBack = onBack,
        onToggleDarkMode = viewModel::toggleDarkMode,
        onToggleNotifications = viewModel::toggleNotifications,
        onToggleHighQualityMode = viewModel::toggleHighQualityMode,
        onToggleDeveloperMode = viewModel::toggleDeveloperMode,
        onVersionTapped = viewModel::onVersionTapped,
    )
}
