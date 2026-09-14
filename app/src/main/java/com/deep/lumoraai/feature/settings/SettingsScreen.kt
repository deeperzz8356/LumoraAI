package com.deep.lumoraai.feature.settings

import android.view.LayoutInflater
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.BuildConfig
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.databinding.SettingsScreenBinding
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleHighQualityMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val user = FirebaseAuth.getInstance().currentUser
    LumoraXmlScreen(
        selectedTab = "settings",
        onNavigate = onNavigate,
        modifier = modifier,
    ) { shell ->
        shell.topBar.visibility = View.GONE
        shell.content.removeAllViews()
        shell.content.setPadding(0, 0, 0, 0)
        val binding = SettingsScreenBinding.inflate(LayoutInflater.from(shell.content.context), shell.content, true)

        binding.languageSubtitle.text = uiState.selectedLanguage.ifBlank { "English" }
        binding.versionText.text = "Version ${BuildConfig.VERSION_NAME}"

        binding.backButton.setOnClickListener { onBack() }
        binding.manageProfileRow.setOnClickListener {
            onNavigate(if (user == null || user.isAnonymous) Screen.Auth.route else Screen.EditProfile.route)
        }
        binding.subscriptionRow.setOnClickListener { onNavigate(Screen.Subscription.route) }
        binding.languageRow.setOnClickListener { onNavigate("${Screen.Language.route}?source=settings") }
    }
}
