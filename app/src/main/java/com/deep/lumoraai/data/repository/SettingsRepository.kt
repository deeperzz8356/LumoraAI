package com.deep.lumoraai.data.repository

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lumora_settings", Context.MODE_PRIVATE)

    var isDarkMode: Boolean
        get() = prefs.getBoolean("dark_mode", true)
        set(value) = prefs.edit().putBoolean("dark_mode", value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()

    var highQualityMode: Boolean
        get() = prefs.getBoolean("high_quality_mode", false)
        set(value) = prefs.edit().putBoolean("high_quality_mode", value).apply()

    /** Stable BCP-47 language tag, never a translated display name. */
    var localeCode: String
        get() = prefs.getString("locale_code", "en") ?: "en"
        set(value) = prefs.edit().putString("locale_code", value).apply()
}
