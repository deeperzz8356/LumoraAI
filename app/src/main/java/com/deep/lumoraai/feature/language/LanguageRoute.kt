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
import com.deep.lumoraai.core.localization.LocaleManager
import android.app.Activity

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
            viewModel.persistSelection()
            val selected = (viewModel.uiState as? LanguageUiState.Success)?.selectedLanguageCode ?: "en"
            // Route through the single coordinator so all three persistence paths
            // (SharedPreferences, DataStore, per-app locale) agree BEFORE any
            // refresh happens (Bug C). The synchronous stores are written before
            // this returns, so attachBaseContext re-reads the fresh locale.
            LocaleManager.setLocale(context, selected)
            // The host is a ComponentActivity (not AppCompatActivity), so it is
            // not auto-recreated by the delegate. Trigger a single, deterministic
            // recreation AFTER the locale is fully applied and persisted, removing
            // the race between the async delegate apply and a manual recreate()
            // (Bug B). attachBaseContext re-reads the freshly persisted locale so
            // the whole UI (RTL included for Arabic) re-renders in the new language.
            (context as? Activity)?.recreate()
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
