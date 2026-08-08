package com.deep.lumoraai.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey

object PreferenceKeys {
    val IS_DEVELOPER_MODE = booleanPreferencesKey("is_developer_mode")
    val DEV_MODE_UNLOCKED = booleanPreferencesKey("dev_mode_unlocked")
}
