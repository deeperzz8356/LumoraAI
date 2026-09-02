package com.deep.lumoraai.core.notification

/**
 * Predefined haptic feedback patterns for different interactions
 */
object HapticFeedback {
    
    /**
     * Light tap - for simple interactions
     */
    val TAP_LIGHT = longArrayOf(
        0,      // No initial delay
        30      // Vibrate for 30ms
    )

    /**
     * Medium tap - for standard interactions
     */
    val TAP_MEDIUM = longArrayOf(
        0,      // No initial delay
        50      // Vibrate for 50ms
    )

    /**
     * Heavy tap - for important interactions
     */
    val TAP_HEAVY = longArrayOf(
        0,      // No initial delay
        100     // Vibrate for 100ms
    )

    /**
     * Double tap - success feedback
     */
    val DOUBLE_TAP = longArrayOf(
        0,      // No initial delay
        30,     // Vibrate 30ms
        50,     // Wait 50ms
        30      // Vibrate 30ms
    )

    /**
     * Triple tap - error or warning
     */
    val TRIPLE_TAP = longArrayOf(
        0,      // No initial delay
        30,     // Vibrate 30ms
        30,     // Wait 30ms
        30,     // Vibrate 30ms
        30,     // Wait 30ms
        30      // Vibrate 30ms
    )

    /**
     * Long press - sustained feedback
     */
    val LONG_PRESS = longArrayOf(
        0,      // No initial delay
        150     // Vibrate for 150ms
    )

    /**
     * Notification pattern - gentle escalation
     */
    val NOTIFICATION = longArrayOf(
        0,      // No initial delay
        50,     // Vibrate 50ms
        100,    // Wait 100ms
        75      // Vibrate 75ms
    )

    /**
     * Success pattern - celebratory
     */
    val SUCCESS = longArrayOf(
        0,      // No initial delay
        50,     // Vibrate 50ms
        50,     // Wait 50ms
        50,     // Vibrate 50ms
        50,     // Wait 50ms
        100     // Vibrate 100ms
    )

    /**
     * Error pattern - warning rhythm
     */
    val ERROR = longArrayOf(
        0,      // No initial delay
        100,    // Vibrate 100ms
        100,    // Wait 100ms
        100,    // Vibrate 100ms
        100,    // Wait 100ms
        200     // Vibrate 200ms
    )

    /**
     * Loading pattern - continuous
     */
    val LOADING = longArrayOf(
        0,      // No initial delay
        100,    // Vibrate 100ms
        200,    // Wait 200ms
        100,    // Vibrate 100ms
        200,    // Wait 200ms
        100     // Vibrate 100ms
    )

    /**
     * Click pattern - UI feedback
     */
    val CLICK = longArrayOf(
        0,      // No initial delay
        10,     // Vibrate 10ms
        20,     // Wait 20ms
        10      // Vibrate 10ms
    )

    /**
     * Bounce pattern - playful feedback
     */
    val BOUNCE = longArrayOf(
        0,      // No initial delay
        50,     // Vibrate 50ms
        75,     // Wait 75ms
        50,     // Vibrate 50ms
        75,     // Wait 75ms
        50      // Vibrate 50ms
    )

    /**
     * Pulse pattern - continuous rhythm
     */
    val PULSE = longArrayOf(
        0,      // No initial delay
        80,     // Vibrate 80ms
        160,    // Wait 160ms
        80,     // Vibrate 80ms
        160     // Wait 160ms
    )

    /**
     * Get pattern for notification priority
     */
    fun getPatternForPriority(priority: String): LongArray {
        return when (priority) {
            NotificationConfig.NOTIFICATION_PRIORITY_HIGH -> ERROR
            NotificationConfig.NOTIFICATION_PRIORITY_MEDIUM -> NOTIFICATION
            NotificationConfig.NOTIFICATION_PRIORITY_LOW -> TAP_LIGHT
            else -> NOTIFICATION
        }
    }

    /**
     * Get pattern for notification type
     */
    fun getPatternForType(type: String): LongArray {
        return when (type) {
            NotificationConfig.NOTIFICATION_TYPE_TASK_COMPLETION -> SUCCESS
            NotificationConfig.NOTIFICATION_TYPE_ENGAGEMENT -> NOTIFICATION
            NotificationConfig.NOTIFICATION_TYPE_FEATURE_ANNOUNCEMENT -> BOUNCE
            else -> NOTIFICATION
        }
    }

    /**
     * Combine two patterns
     */
    fun combinePatterns(pattern1: LongArray, pattern2: LongArray): LongArray {
        val combined = mutableListOf<Long>()
        combined.addAll(pattern1.toList())
        combined.addAll(pattern2.drop(1)) // Skip the initial delay from pattern2
        return combined.toLongArray()
    }
}
