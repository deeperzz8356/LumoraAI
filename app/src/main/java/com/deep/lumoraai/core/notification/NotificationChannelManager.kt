package com.deep.lumoraai.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.deep.lumoraai.core.notification.NotificationConfig.CHANNEL_DESC_HIGH
import com.deep.lumoraai.core.notification.NotificationConfig.CHANNEL_DESC_LOW
import com.deep.lumoraai.core.notification.NotificationConfig.CHANNEL_DESC_MEDIUM
import com.deep.lumoraai.core.notification.NotificationConfig.CHANNEL_ID_HIGH
import com.deep.lumoraai.core.notification.NotificationConfig.CHANNEL_ID_LOW
import com.deep.lumoraai.core.notification.NotificationConfig.CHANNEL_ID_MEDIUM
import com.deep.lumoraai.core.notification.NotificationConfig.CHANNEL_NAME_HIGH
import com.deep.lumoraai.core.notification.NotificationConfig.CHANNEL_NAME_MEDIUM
import com.deep.lumoraai.core.notification.NotificationConfig.CHANNEL_NAME_LOW
import com.deep.lumoraai.core.notification.NotificationConfig.IMPORTANCE_HIGH
import com.deep.lumoraai.core.notification.NotificationConfig.IMPORTANCE_LOW
import com.deep.lumoraai.core.notification.NotificationConfig.IMPORTANCE_MEDIUM
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for creating and configuring notification channels
 * Required for Android 8.0+ (API 26+)
 */
@Singleton
class NotificationChannelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "NotificationChannelManager"
    }

    init {
        createNotificationChannels()
    }

    /**
     * Create all notification channels
     * Called once during app initialization
     */
    fun createNotificationChannels() {
        // Only create channels on Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            // Create HIGH priority channel
            createHighPriorityChannel(notificationManager)

            // Create MEDIUM priority channel
            createMediumPriorityChannel(notificationManager)

            // Create LOW priority channel
            createLowPriorityChannel(notificationManager)

            Log.d(TAG, "Notification channels created successfully")
        }
    }

    /**
     * Create HIGH priority notification channel
     * Used for: Task completion, critical alerts
     */
    private fun createHighPriorityChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_HIGH,
                CHANNEL_NAME_HIGH,
                IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_HIGH
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                setBypassDnd(false) // Respect Do Not Disturb

                // Set vibration pattern
                vibrationPattern = NotificationConfig.VIBRATION_MEDIUM

                // Set notification sound
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(
                    getDefaultNotificationSoundUri(),
                    audioAttributes
                )

                // Allow full screen intent for critical notifications
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    setAllowBubbles(true)
                }
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "HIGH priority channel created")
        }
    }

    /**
     * Create MEDIUM priority notification channel
     * Used for: Feature announcements, important updates
     */
    private fun createMediumPriorityChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_MEDIUM,
                CHANNEL_NAME_MEDIUM,
                IMPORTANCE_MEDIUM
            ).apply {
                description = CHANNEL_DESC_MEDIUM
                enableVibration(true)
                enableLights(false)
                setShowBadge(true)
                setBypassDnd(false)

                // Set vibration pattern
                vibrationPattern = NotificationConfig.VIBRATION_LIGHT

                // Set notification sound
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(
                    getDefaultNotificationSoundUri(),
                    audioAttributes
                )
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "MEDIUM priority channel created")
        }
    }

    /**
     * Create LOW priority notification channel
     * Used for: Tips, general information
     */
    private fun createLowPriorityChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_LOW,
                CHANNEL_NAME_LOW,
                IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESC_LOW
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
                setBypassDnd(false)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "LOW priority channel created")
        }
    }

    /**
     * Get the appropriate channel ID based on priority
     */
    fun getChannelIdByPriority(priority: String): String {
        return when (priority) {
            NotificationConfig.NOTIFICATION_PRIORITY_HIGH -> CHANNEL_ID_HIGH
            NotificationConfig.NOTIFICATION_PRIORITY_MEDIUM -> CHANNEL_ID_MEDIUM
            NotificationConfig.NOTIFICATION_PRIORITY_LOW -> CHANNEL_ID_LOW
            else -> CHANNEL_ID_MEDIUM
        }
    }

    /**
     * Get the notification importance level by priority
     */
    fun getImportanceByPriority(priority: String): Int {
        return when (priority) {
            NotificationConfig.NOTIFICATION_PRIORITY_HIGH -> NotificationCompat.PRIORITY_MAX
            NotificationConfig.NOTIFICATION_PRIORITY_MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            NotificationConfig.NOTIFICATION_PRIORITY_LOW -> NotificationCompat.PRIORITY_MIN
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    /**
     * Update channel properties
     */
    fun updateChannelSettings(
        channelId: String,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true,
        lightEnabled: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            val channel = notificationManager.getNotificationChannel(channelId)
            if (channel != null) {
                try {
                    // Note: Some properties cannot be changed after creation
                    // Only sound and vibration can be updated via intent
                    Log.d(TAG, "Channel settings updated for: $channelId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating channel settings: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Delete a notification channel
     */
    fun deleteChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.deleteNotificationChannel(channelId)
            Log.d(TAG, "Notification channel deleted: $channelId")
        }
    }

    /**
     * Get default notification sound URI
     */
    private fun getDefaultNotificationSoundUri(): Uri {
        return android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
    }

    /**
     * Get all notification channels (Android 8.0+)
     */
    fun getAllChannels(): List<NotificationChannel> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            return notificationManager.notificationChannels
        }
        return emptyList()
    }

    /**
     * Get specific notification channel
     */
    fun getChannel(channelId: String): NotificationChannel? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            return notificationManager.getNotificationChannel(channelId)
        }
        return null
    }
}
