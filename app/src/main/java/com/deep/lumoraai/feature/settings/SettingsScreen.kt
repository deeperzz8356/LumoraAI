package com.deep.lumoraai.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.BuildConfig
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addActionRow
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.addToggleRow
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.core.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleHighQualityMode: (Boolean) -> Unit,
    onToggleDeveloperMode: (Boolean) -> Unit,
    onVersionTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    LumoraXmlScreen(
        selectedTab = "settings",
        onNavigate = onNavigate,
        modifier = modifier
    ) { binding ->
        binding.setupTopBar(
            titleText = binding.root.context.getString(R.string.settings),
            subtitleText = "Account, preferences and app controls",
            onBack = onBack
        )
        binding.resetContent()
        val user = FirebaseAuth.getInstance().currentUser

        binding.content.addSectionTitle("Account")
        binding.content.addActionRow(
            title = "Manage Profile",
            subtitle = if (user == null || user.isAnonymous) "Sign in to edit your creator profile" else "Edit your name, avatar and profile details",
            icon = R.drawable.ic_lumora_avatar,
            onClick = { onNavigate(if (user == null || user.isAnonymous) Screen.Auth.route else Screen.EditProfile.route) }
        )
        binding.content.addActionRow(
            title = "Subscription & Billing",
            subtitle = "Manage Lumora Pro and credit purchases",
            icon = R.drawable.ic_lumora_star,
            onClick = { onNavigate(Screen.Subscription.route) }
        )

        binding.content.addSectionTitle("Preferences")
        binding.content.addActionRow(
            title = "Language",
            subtitle = uiState.selectedLanguage,
            icon = R.drawable.ic_lumora_settings,
            onClick = { onNavigate("${Screen.Language.route}?source=settings") }
        )
        binding.content.addToggleRow("Notifications", "Receive generation and account alerts", uiState.notificationsEnabled, onToggleNotifications)
        /* Hidden from the XML-backed Account Settings screen by product request.
        binding.content.addToggleRow("High Quality Mode", "Prefer quality over speed where supported", uiState.highQualityMode, onToggleHighQualityMode)
        binding.content.addToggleRow("Dark Mode", "Lumora uses the dark studio theme", uiState.isDarkMode, onToggleDarkMode)

        if (uiState.isDevModeUnlocked) {
            binding.content.addSectionTitle("Developer")
            binding.content.addToggleRow("Developer Mode", "Unlimited local testing credits and tools", uiState.isDeveloperMode, onToggleDeveloperMode)
        }

        val unlockHint = if (BuildConfig.DEBUG && !uiState.isDevModeUnlocked && uiState.versionTapCount > 0) {
            "${7 - uiState.versionTapCount} taps to unlock developer mode"
        } else {
            null
        }
        binding.content.addTextCard("Version ${BuildConfig.VERSION_NAME}", unlockHint) {
            onVersionTapped()
        }
        */
    }
}
