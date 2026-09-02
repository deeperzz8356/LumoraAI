package com.deep.lumoraai.core.notification

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Demo notification helper class to easily trigger test notifications
 * Use this to test the notification system during development
 */
object NotificationDemo {
    private const val TAG = "NotificationDemo"

    /**
     * Trigger a job completion notification
     */
    fun triggerJobCompletion(
        notificationManager: NotificationManager,
        scope: CoroutineScope,
        taskName: String = "AI Generation Task"
    ) {
        scope.launch {
            try {
                notificationManager.sendTaskCompletionNotification(
                    title = "✓ $taskName Complete",
                    message = "Your task has been processed successfully. Ready to download or share!",
                    imageUrl = null,
                    actionUrl = "com.deep.lumoraai://result"
                )
                Log.d(TAG, "Job completion notification triggered")
            } catch (e: Exception) {
                Log.e(TAG, "Error triggering job completion notification", e)
            }
        }
    }

    /**
     * Trigger an engagement notification (reminder or alert)
     */
    fun triggerEngagementNotification(
        notificationManager: NotificationManager,
        scope: CoroutineScope,
        title: String = "Daily Reminder",
        message: String = "You have pending creations waiting. Finish them now!"
    ) {
        scope.launch {
            try {
                notificationManager.sendEngagementNotification(
                    title = title,
                    message = message,
                    imageUrl = null,
                    actionUrl = "com.deep.lumoraai://queue"
                )
                Log.d(TAG, "Engagement notification triggered")
            } catch (e: Exception) {
                Log.e(TAG, "Error triggering engagement notification", e)
            }
        }
    }

    /**
     * Trigger a feature announcement
     */
    fun triggerFeatureAnnouncement(
        notificationManager: NotificationManager,
        scope: CoroutineScope,
        featureName: String = "New Feature"
    ) {
        scope.launch {
            try {
                notificationManager.sendFeatureAnnouncement(
                    title = "🎉 $featureName is Here!",
                    message = "We've just launched a new feature to help you create faster. Try it now!",
                    imageUrl = null,
                    actionUrl = "com.deep.lumoraai://createhub"
                )
                Log.d(TAG, "Feature announcement notification triggered")
            } catch (e: Exception) {
                Log.e(TAG, "Error triggering feature announcement", e)
            }
        }
    }

    /**
     * Trigger an error notification
     */
    fun triggerErrorNotification(
        notificationManager: NotificationManager,
        scope: CoroutineScope,
        errorTitle: String = "Processing Error",
        errorMessage: String = "Your task failed. Please try again."
    ) {
        scope.launch {
            try {
                notificationManager.sendErrorNotification(
                    title = "⚠️ $errorTitle",
                    message = errorMessage,
                    actionUrl = "com.deep.lumoraai://queue"
                )
                Log.d(TAG, "Error notification triggered")
            } catch (e: Exception) {
                Log.e(TAG, "Error triggering error notification", e)
            }
        }
    }

    /**
     * Trigger a batch of demo notifications
     * Useful for testing the notification system
     */
    fun triggerDemoBatch(
        notificationManager: NotificationManager,
        scope: CoroutineScope
    ) {
        scope.launch {
            try {
                // Task completion
                notificationManager.sendTaskCompletionNotification(
                    title = "✓ Image Generation Complete",
                    message = "Your AI-generated image is ready! Stunning quality!",
                    actionUrl = "com.deep.lumoraai://result"
                )

                // Engagement
                notificationManager.sendEngagementNotification(
                    title = "You have 3 pending videos",
                    message = "Complete your video generation queue now"
                )

                // Feature announcement
                notificationManager.sendFeatureAnnouncement(
                    title = "🎉 Batch Processing Ready",
                    message = "Process multiple images at once and save time!",
                    actionUrl = "com.deep.lumoraai://createhub"
                )

                // Custom notification
                notificationManager.sendCustomNotification(
                    title = "💎 Premium Features Available",
                    message = "Unlock unlimited generations with premium plan",
                    type = "PROMOTIONAL",
                    priority = "MEDIUM",
                    actionUrl = "com.deep.lumoraai://subscription"
                )

                Log.d(TAG, "Demo batch notifications triggered")
            } catch (e: Exception) {
                Log.e(TAG, "Error triggering demo batch", e)
            }
        }
    }

    /**
     * Simulate a series of notifications (like a job processing workflow)
     */
    fun simulateJobWorkflow(
        notificationManager: NotificationManager,
        scope: CoroutineScope
    ) {
        scope.launch {
            try {
                // Start notification
                notificationManager.sendCustomNotification(
                    title = "🚀 Processing Started",
                    message = "Your AI transformation is underway...",
                    type = "PROGRESS",
                    priority = "MEDIUM"
                )

                // Simulate delays between steps
                kotlinx.coroutines.delay(2000)

                // Progress update
                notificationManager.sendCustomNotification(
                    title = "⚙️ Processing (50%)",
                    message = "Halfway there! Keep the app open.",
                    type = "PROGRESS",
                    priority = "MEDIUM"
                )

                kotlinx.coroutines.delay(2000)

                // Completion
                notificationManager.sendTaskCompletionNotification(
                    title = "✓ Complete!",
                    message = "Your creation is ready to download",
                    actionUrl = "com.deep.lumoraai://result"
                )

                Log.d(TAG, "Job workflow simulation completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error in job workflow simulation", e)
            }
        }
    }
}
