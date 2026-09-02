package com.deep.lumoraai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.deep.lumoraai.data.local.PreferenceKeys
import com.deep.lumoraai.domain.model.NotificationFrequencies
import com.deep.lumoraai.domain.model.NotificationPreferences
import com.deep.lumoraai.domain.model.NotificationSounds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "notification_preferences"
)

@Singleton
class NotificationPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.notificationPreferencesDataStore

    /**
     * Get all notification preferences as a Flow
     */
    fun getNotificationPreferences(): Flow<NotificationPreferences> {
        return dataStore.data.map { preferences ->
            NotificationPreferences(
                notificationsEnabled = preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true,
                taskCompletionNotifications = preferences[PreferenceKeys.TASK_COMPLETION_NOTIFICATIONS] ?: true,
                engagementNotifications = preferences[PreferenceKeys.ENGAGEMENT_NOTIFICATIONS] ?: true,
                featureAnnouncementNotifications = preferences[PreferenceKeys.FEATURE_ANNOUNCEMENT_NOTIFICATIONS] ?: true,
                soundEnabled = preferences[PreferenceKeys.SOUND_ENABLED] ?: true,
                vibrationEnabled = preferences[PreferenceKeys.VIBRATION_ENABLED] ?: true,
                notificationSound = preferences[PreferenceKeys.NOTIFICATION_SOUND] ?: "default",
                doNotDisturbEnabled = preferences[PreferenceKeys.DO_NOT_DISTURB_ENABLED] ?: false,
                doNotDisturbStartHour = preferences[PreferenceKeys.DO_NOT_DISTURB_START_HOUR] ?: 22,
                doNotDisturbEndHour = preferences[PreferenceKeys.DO_NOT_DISTURB_END_HOUR] ?: 8,
                notificationFrequency = preferences[PreferenceKeys.NOTIFICATION_FREQUENCY] ?: "instant",
                maxNotificationsPerDay = preferences[PreferenceKeys.MAX_NOTIFICATIONS_PER_DAY] ?: 20
            )
        }
    }

    /**
     * Toggle all notifications on/off
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    /**
     * Toggle task completion notifications
     */
    suspend fun setTaskCompletionNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.TASK_COMPLETION_NOTIFICATIONS] = enabled
        }
    }

    /**
     * Toggle engagement notifications
     */
    suspend fun setEngagementNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.ENGAGEMENT_NOTIFICATIONS] = enabled
        }
    }

    /**
     * Toggle feature announcement notifications
     */
    suspend fun setFeatureAnnouncementNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.FEATURE_ANNOUNCEMENT_NOTIFICATIONS] = enabled
        }
    }

    /**
     * Toggle sound for notifications
     */
    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SOUND_ENABLED] = enabled
        }
    }

    /**
     * Toggle vibration for notifications
     */
    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.VIBRATION_ENABLED] = enabled
        }
    }

    /**
     * Set notification sound
     */
    suspend fun setNotificationSound(sound: String) {
        if (!NotificationSounds.isValid(sound)) {
            throw IllegalArgumentException("Invalid sound: $sound")
        }
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATION_SOUND] = sound
        }
    }

    /**
     * Toggle Do Not Disturb mode
     */
    suspend fun setDoNotDisturbEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.DO_NOT_DISTURB_ENABLED] = enabled
        }
    }

    /**
     * Set Do Not Disturb start hour (24-hour format)
     */
    suspend fun setDoNotDisturbStartHour(hour: Int) {
        if (hour !in 0..23) {
            throw IllegalArgumentException("Hour must be between 0 and 23")
        }
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.DO_NOT_DISTURB_START_HOUR] = hour
        }
    }

    /**
     * Set Do Not Disturb end hour (24-hour format)
     */
    suspend fun setDoNotDisturbEndHour(hour: Int) {
        if (hour !in 0..23) {
            throw IllegalArgumentException("Hour must be between 0 and 23")
        }
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.DO_NOT_DISTURB_END_HOUR] = hour
        }
    }

    /**
     * Set notification frequency
     */
    suspend fun setNotificationFrequency(frequency: String) {
        if (!NotificationFrequencies.isValid(frequency)) {
            throw IllegalArgumentException("Invalid frequency: $frequency")
        }
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATION_FREQUENCY] = frequency
        }
    }

    /**
     * Set maximum notifications per day
     */
    suspend fun setMaxNotificationsPerDay(max: Int) {
        if (max <= 0) {
            throw IllegalArgumentException("Max notifications must be greater than 0")
        }
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.MAX_NOTIFICATIONS_PER_DAY] = max
        }
    }

    /**
     * Reset all preferences to defaults
     */
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] = true
            preferences[PreferenceKeys.TASK_COMPLETION_NOTIFICATIONS] = true
            preferences[PreferenceKeys.ENGAGEMENT_NOTIFICATIONS] = true
            preferences[PreferenceKeys.FEATURE_ANNOUNCEMENT_NOTIFICATIONS] = true
            preferences[PreferenceKeys.SOUND_ENABLED] = true
            preferences[PreferenceKeys.VIBRATION_ENABLED] = true
            preferences[PreferenceKeys.NOTIFICATION_SOUND] = "default"
            preferences[PreferenceKeys.DO_NOT_DISTURB_ENABLED] = false
            preferences[PreferenceKeys.DO_NOT_DISTURB_START_HOUR] = 22
            preferences[PreferenceKeys.DO_NOT_DISTURB_END_HOUR] = 8
            preferences[PreferenceKeys.NOTIFICATION_FREQUENCY] = "instant"
            preferences[PreferenceKeys.MAX_NOTIFICATIONS_PER_DAY] = 20
        }
    }

    /**
     * Check if notification should be shown based on current preferences and time
     */
    suspend fun shouldShowNotification(
        type: String // "TASK_COMPLETION", "ENGAGEMENT", "FEATURE_ANNOUNCEMENT"
    ): Boolean {
        val prefs = getNotificationPreferences().first()

        // Check if all notifications are disabled
        if (!prefs.notificationsEnabled) return false

        // Check if Do Not Disturb is active
        if (isInDoNotDisturbTime(prefs)) return false

        // Check notification type
        return when (type) {
            "TASK_COMPLETION" -> prefs.taskCompletionNotifications
            "ENGAGEMENT" -> prefs.engagementNotifications
            "FEATURE_ANNOUNCEMENT" -> prefs.featureAnnouncementNotifications
            else -> true
        }
    }

    /**
     * Check if current time is within Do Not Disturb hours
     */
    private fun isInDoNotDisturbTime(prefs: NotificationPreferences): Boolean {
        if (!prefs.doNotDisturbEnabled) return false

        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

        return if (prefs.doNotDisturbStartHour <= prefs.doNotDisturbEndHour) {
            // Normal case: e.g., 22 to 8 (10 PM to 8 AM)
            currentHour >= prefs.doNotDisturbStartHour || currentHour < prefs.doNotDisturbEndHour
        } else {
            // Wraps around midnight: handled by the normal case above
            currentHour >= prefs.doNotDisturbStartHour || currentHour < prefs.doNotDisturbEndHour
        }
    }
}
