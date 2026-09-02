package com.deep.lumoraai.core.notification

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages sound and vibration feedback for notifications
 */
@Singleton
class SoundAndVibrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: com.deep.lumoraai.data.repository.NotificationPreferencesRepository
) {
    companion object {
        private const val TAG = "SoundAndVibrationManager"
        private const val SOUND_POOL_MAX_STREAMS = 5
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = ContextCompat.getSystemService(context, VibratorManager::class.java)
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        ContextCompat.getSystemService(context, Vibrator::class.java)
    }

    private val soundPool: SoundPool? = try {
        SoundPool.Builder()
            .setMaxStreams(SOUND_POOL_MAX_STREAMS)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    } catch (e: Exception) {
        Log.e(TAG, "Error creating SoundPool: ${e.message}")
        null
    }

    private var soundIds = mutableMapOf<String, Int>()

    init {
        loadSounds()
    }

    /**
     * Load notification sounds into SoundPool
     */
    private fun loadSounds() {
        try {
            // Note: Attempting to load from app resources
            // Since there's no custom notification.mp3, we skip SoundPool loading
            // and rely on system notification sounds via RingtoneManager
            Log.d(TAG, "Notification sounds will use system default")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sounds: ${e.message}", e)
        }
    }

    /**
     * Play notification sound based on user preferences and priority
     */
    fun playNotificationSound(
        priority: String = NotificationConfig.NOTIFICATION_PRIORITY_MEDIUM,
        soundType: String = NotificationConfig.SOUND_DEFAULT
    ) {
        scope.launch {
            try {
                // Check if sounds are enabled in preferences
                val prefs = preferencesRepository.getNotificationPreferences().collect { prefs ->
                    if (prefs.soundEnabled) {
                        playSound(soundType)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing notification sound: ${e.message}", e)
            }
        }
    }

    /**
     * Play a specific sound
     */
    private fun playSound(soundType: String) {
        try {
            val soundId = when (soundType) {
                NotificationConfig.SOUND_NONE -> return
                NotificationConfig.SOUND_DEFAULT -> soundIds[NotificationConfig.SOUND_DEFAULT] ?: -1
                NotificationConfig.SOUND_ALERT -> soundIds[NotificationConfig.SOUND_ALERT] ?: -1
                NotificationConfig.SOUND_NOTIFICATION -> soundIds[NotificationConfig.SOUND_NOTIFICATION] ?: -1
                else -> soundIds[NotificationConfig.SOUND_DEFAULT] ?: -1
            }

            if (soundId > 0 && soundPool != null) {
                soundPool.play(
                    soundId,
                    0.8f,  // Left volume
                    0.8f,  // Right volume
                    1,     // Priority
                    0,     // Loop (0 = no loop)
                    1f     // Rate
                )
                Log.d(TAG, "Playing sound: $soundType")
            } else {
                playDefaultNotificationSound()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}", e)
            // Fallback to default system notification sound
            playDefaultNotificationSound()
        }
    }

    /**
     * Play system default notification sound
     */
    private fun playDefaultNotificationSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
            Log.d(TAG, "Playing default notification sound")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing default notification sound: ${e.message}", e)
        }
    }

    /**
     * Vibrate device based on priority
     */
    fun vibrateForNotification(
        priority: String = NotificationConfig.NOTIFICATION_PRIORITY_MEDIUM
    ) {
        scope.launch {
            try {
                // Check if vibration is enabled
                val prefs = preferencesRepository.getNotificationPreferences().collect { prefs ->
                    if (prefs.vibrationEnabled && hasVibratorPermission()) {
                        val pattern = when (priority) {
                            NotificationConfig.NOTIFICATION_PRIORITY_HIGH -> {
                                NotificationConfig.VIBRATION_STRONG
                            }
                            NotificationConfig.NOTIFICATION_PRIORITY_MEDIUM -> {
                                NotificationConfig.VIBRATION_MEDIUM
                            }
                            NotificationConfig.NOTIFICATION_PRIORITY_LOW -> {
                                NotificationConfig.VIBRATION_LIGHT
                            }
                            else -> NotificationConfig.VIBRATION_MEDIUM
                        }
                        vibrate(pattern)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error vibrating device: ${e.message}", e)
            }
        }
    }

    /**
     * Custom vibration pattern
     * @param pattern Array of milliseconds [delay, vibrate, delay, vibrate, ...]
     */
    fun vibrate(pattern: LongArray) {
        try {
            if (!hasVibratorPermission()) {
                Log.w(TAG, "Vibrator permission not granted")
                return
            }

            if (vibrator == null) {
                Log.w(TAG, "Vibrator not available")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val effect = VibrationEffect.createWaveform(pattern, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }

            Log.d(TAG, "Vibration pattern played: ${pattern.joinToString(", ")}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating vibration: ${e.message}", e)
        }
    }

    /**
     * Single vibration (simple tap)
     */
    fun vibrateOnce(durationMs: Long = 100) {
        try {
            if (!hasVibratorPermission()) return
            if (vibrator == null) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val effect = VibrationEffect.createOneShot(
                    durationMs,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }

            Log.d(TAG, "Single vibration: ${durationMs}ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating once: ${e.message}", e)
        }
    }

    /**
     * Play combined sound and vibration
     */
    fun playNotificationFeedback(
        priority: String = NotificationConfig.NOTIFICATION_PRIORITY_MEDIUM,
        soundType: String = NotificationConfig.SOUND_DEFAULT
    ) {
        playNotificationSound(priority, soundType)
        vibrateForNotification(priority)
    }

    /**
     * Check if device has vibrator
     */
    fun hasVibrator(): Boolean = vibrator?.hasVibrator() ?: false

    /**
     * Check if vibrator permission is granted
     */
    private fun hasVibratorPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.VIBRATE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Cleanup resources
     */
    fun release() {
        try {
            soundPool?.release()
            Log.d(TAG, "SoundPool released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing SoundPool: ${e.message}", e)
        }
    }

    /**
     * Enable/disable all sounds
     */
    fun setSoundEnabled(enabled: Boolean) {
        scope.launch {
            try {
                preferencesRepository.setSoundEnabled(enabled)
                Log.d(TAG, "Sound ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting sound preference: ${e.message}", e)
            }
        }
    }

    /**
     * Enable/disable vibration
     */
    fun setVibrationEnabled(enabled: Boolean) {
        scope.launch {
            try {
                preferencesRepository.setVibrationEnabled(enabled)
                Log.d(TAG, "Vibration ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting vibration preference: ${e.message}", e)
            }
        }
    }

    /**
     * Set notification sound type
     */
    fun setNotificationSound(soundType: String) {
        scope.launch {
            try {
                preferencesRepository.setNotificationSound(soundType)
                Log.d(TAG, "Notification sound set to: $soundType")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting notification sound: ${e.message}", e)
            }
        }
    }
}
