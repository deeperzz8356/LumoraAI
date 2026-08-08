package com.deep.lumoraai.feature.settings

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val highQualityMode: Boolean = false,
    val selectedLanguage: String = "English",
    val isDeveloperMode: Boolean = false,
    val isDevModeUnlocked: Boolean = false,
    val versionTapCount: Int = 0
)
