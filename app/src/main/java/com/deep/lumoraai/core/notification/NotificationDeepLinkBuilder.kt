package com.deep.lumoraai.core.notification

import com.deep.lumoraai.core.deeplink.DeepLinkDestination

/**
 * Helper class to build appropriate deep links for different notification types
 */
object NotificationDeepLinkBuilder {

    /**
     * Build a deep link for a task completion notification
     */
    fun buildTaskNotificationDeepLink(taskId: String): String {
        return DeepLinkDestination.Task(taskId).toUri()
    }

    /**
     * Build a deep link for a feature announcement notification
     */
    fun buildFeatureNotificationDeepLink(featureId: String): String {
        return DeepLinkDestination.Feature(featureId).toUri()
    }

    /**
     * Build a deep link for a result notification
     */
    fun buildResultNotificationDeepLink(resultId: String): String {
        return DeepLinkDestination.Result(resultId).toUri()
    }

    /**
     * Build a deep link for an engagement/reminder notification
     */
    fun buildEngagementNotificationDeepLink(): String {
        return DeepLinkDestination.Notifications.toUri()
    }

    /**
     * Build a deep link based on notification type and data
     */
    fun buildDeepLink(
        notificationType: String,
        data: Map<String, String>?
    ): String? {
        return when (notificationType) {
            "TASK_COMPLETION" -> {
                val taskId = data?.get("task_id")
                if (!taskId.isNullOrEmpty()) {
                    buildTaskNotificationDeepLink(taskId)
                } else {
                    null
                }
            }
            "FEATURE_ANNOUNCEMENT" -> {
                val featureId = data?.get("feature_id")
                if (!featureId.isNullOrEmpty()) {
                    buildFeatureNotificationDeepLink(featureId)
                } else {
                    null
                }
            }
            "ENGAGEMENT" -> buildEngagementNotificationDeepLink()
            else -> null
        }
    }
}
