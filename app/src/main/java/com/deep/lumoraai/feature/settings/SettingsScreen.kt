package com.deep.lumoraai.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.BuildConfig
import com.deep.lumoraai.core.components.PremiumActionRow
import com.deep.lumoraai.core.components.PremiumHero
import com.deep.lumoraai.core.components.PremiumPage
import com.deep.lumoraai.core.components.PremiumSection
import com.deep.lumoraai.core.components.PremiumToggleRow
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
    modifier: Modifier = Modifier,
) {
    val user = FirebaseAuth.getInstance().currentUser
    PremiumPage("Settings", "Your workspace", "Account, preferences and app controls", onBack, modifier) {
        PremiumHero("Studio controls", "Make Lumora work your way", "Keep your account, creative preferences and privacy choices in one place.", Icons.Default.AutoAwesome)
        PremiumSection("Account", "Identity, plan and billing") {
            PremiumActionRow(
                "Manage profile",
                if (user == null || user.isAnonymous) "Sign in to edit your creator profile" else "Edit your name, avatar and creator details",
                Icons.Default.AccountCircle,
                { onNavigate(if (user == null || user.isAnonymous) Screen.Auth.route else Screen.EditProfile.route) },
            )
            PremiumActionRow("Subscription & billing", "Manage Lumora Pro and credit purchases", Icons.Default.CreditCard, { onNavigate(Screen.Subscription.route) })
        }
        PremiumSection("Preferences", "Tune the everyday experience") {
            PremiumActionRow("Language", uiState.selectedLanguage, Icons.Default.Language, { onNavigate("${Screen.Language.route}?source=settings") }, trailing = uiState.selectedLanguage.uppercase())
            PremiumToggleRow("Notifications", "Receive generation and account alerts", Icons.Default.Notifications, uiState.notificationsEnabled, onToggleNotifications)
            PremiumToggleRow("High quality mode", "Prefer quality over speed where supported", Icons.Default.AutoAwesome, uiState.highQualityMode, onToggleHighQualityMode)
            PremiumToggleRow("Dark studio", "Use Lumora's focused dark workspace", Icons.Default.DarkMode, uiState.isDarkMode, onToggleDarkMode)
        }
        PremiumSection("Trust & support") {
            PremiumActionRow("Privacy & security", "Control data and account protection", Icons.Default.Security, { onNavigate(Screen.PrivacySecurity.route) })
            PremiumActionRow("Help & support", "Guides, contact options and service status", Icons.Default.SupportAgent, { onNavigate(Screen.HelpSupport.route) })
        }
        if (uiState.isDevModeUnlocked) {
            PremiumSection("Developer") {
                PremiumToggleRow("Developer mode", "Unlimited local testing credits and tools", Icons.Default.AutoAwesome, uiState.isDeveloperMode, onToggleDeveloperMode)
            }
        }
        PremiumActionRow(
            "Lumora ${BuildConfig.VERSION_NAME}",
            if (BuildConfig.DEBUG && !uiState.isDevModeUnlocked && uiState.versionTapCount > 0) "${7 - uiState.versionTapCount} taps to unlock developer mode" else "App version and build information",
            Icons.Default.Info,
            onVersionTapped,
            trailing = "BUILD",
        )
    }
}
