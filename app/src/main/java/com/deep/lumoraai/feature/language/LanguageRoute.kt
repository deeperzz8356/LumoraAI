package com.deep.lumoraai.feature.language

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LanguageRoute(
    onNext: () -> Unit,
    viewModel: LanguageViewModel = viewModel()
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { onNext() }

    LanguageScreen(
        uiState = viewModel.uiState,
        onLanguageSelected = viewModel::selectLanguage,
        onSearchQueryChanged = viewModel::updateSearchQuery,
        onDone = {
            checkAndRequestNotificationPermission(
                context = context,
                onGranted = onNext,
                onRequest = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            )
        }
    )
}

private fun checkAndRequestNotificationPermission(
    context: android.content.Context,
    onGranted: () -> Unit,
    onRequest: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) onGranted() else onRequest()
    } else {
        onGranted()
    }
}
