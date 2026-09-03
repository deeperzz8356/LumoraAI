package com.deep.lumoraai.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferenceKeys {
    val IS_DEVELOPER_MODE = booleanPreferencesKey("is_developer_mode")
    val DEV_MODE_UNLOCKED = booleanPreferencesKey("dev_mode_unlocked")

    // Notification Preferences
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val TASK_COMPLETION_NOTIFICATIONS = booleanPreferencesKey("task_completion_notifications")
    val ENGAGEMENT_NOTIFICATIONS = booleanPreferencesKey("engagement_notifications")
    val FEATURE_ANNOUNCEMENT_NOTIFICATIONS = booleanPreferencesKey("feature_announcement_notifications")
    
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    val NOTIFICATION_SOUND = stringPreferencesKey("notification_sound")
    
    val DO_NOT_DISTURB_ENABLED = booleanPreferencesKey("do_not_disturb_enabled")
    val DO_NOT_DISTURB_START_HOUR = intPreferencesKey("do_not_disturb_start_hour")
    val DO_NOT_DISTURB_END_HOUR = intPreferencesKey("do_not_disturb_end_hour")
    
    val NOTIFICATION_FREQUENCY = stringPreferencesKey("notification_frequency") // "instant", "daily", "weekly"
    val MAX_NOTIFICATIONS_PER_DAY = intPreferencesKey("max_notifications_per_day")
    val LOCALE_CODE = stringPreferencesKey("locale_code")
}
