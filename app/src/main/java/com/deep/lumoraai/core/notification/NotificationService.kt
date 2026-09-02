package com.deep.lumoraai.core.notification

import android.util.Log
import com.deep.lumoraai.data.repository.NotificationRepository
import com.deep.lumoraai.domain.model.Notification
import com.deep.lumoraai.domain.model.NotificationPriority
import com.deep.lumoraai.domain.model.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central notification service for managing all notification operations
 * Provides a clean API for the rest of the app to use
 */
@Singleton
class NotificationService @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val notificationHandler: NotificationHandler,
    private val oneSignalManager: OneSignalManager
) {
    companion object {
        private const val TAG = "NotificationService"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    // ============ Data Access ============

    /**
     * Get all notifications as a Flow
     */
    fun getAllNotifications(): Flow<List<Notification>> {
        return notificationRepository.getAllNotifications()
    }

    /**
     * Get unread notifications
     */
    fun getUnreadNotifications(): Flow<List<Notification>> {
        return notificationRepository.getUnreadNotifications()
    }

    /**
     * Get notifications by type
     */
    fun getNotificationsByType(type: NotificationType): Flow<List<Notification>> {
        return notificationRepository.getNotificationsByType(type.name)
    }

    /**
     * Get notifications by priority
     */
    fun getNotificationsByPriority(priority: NotificationPriority): Flow<List<Notification>> {
        return notificationRepository.getNotificationsByPriority(priority.name)
    }

    /**
     * Get unread notification count
     */
    fun getUnreadCount(): Flow<Int> {
        return notificationRepository.getUnreadCount()
    }

    /**
     * Get a single notification by ID
     */
    suspend fun getNotificationById(id: String): Notification? {
        return notificationRepository.getNotificationById(id)
    }

    // ============ Notification Handlers ============

    /**
     * Send task completion notification
     */
    fun sendTaskCompletionNotification(
        taskId: String,
        taskTitle: String,
        taskDescription: String,
        imageUrl: String? = null
    ) {
        scope.launch {
            try {
                notificationHandler.handleTaskCompletion(
                    taskId = taskId,
                    taskTitle = taskTitle,
                    taskDescription = taskDescription,
                    imageUrl = imageUrl
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error sending task completion notification: ${e.message}", e)
            }
        }
    }

    /**
     * Send engagement notification (reminders, summaries, etc.)
     */
    fun sendEngagementNotification(
        engagementType: String,
        title: String,
        message: String
    ) {
        scope.launch {
            try {
                notificationHandler.handleEngagementNotification(
                    engagementType = engagementType,
                    title = title,
                    message = message
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error sending engagement notification: ${e.message}", e)
            }
        }
    }

    /**
     * Send feature announcement notification
     */
    fun sendFeatureAnnouncement(
        featureId: String,
        featureName: String,
        featureDescription: String,
        imageUrl: String? = null
    ) {
        scope.launch {
            try {
                notificationHandler.handleFeatureAnnouncement(
                    featureId = featureId,
                    featureName = featureName,
                    featureDescription = featureDescription,
                    imageUrl = imageUrl
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error sending feature announcement: ${e.message}", e)
            }
        }
    }

    /**
     * Send error notification
     */
    fun sendErrorNotification(
        errorCode: String,
        errorMessage: String,
        actionUrl: String? = null
    ) {
        scope.launch {
            try {
                notificationHandler.handleErrorNotification(
                    errorCode = errorCode,
                    errorMessage = errorMessage,
                    actionUrl = actionUrl
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error sending error notification: ${e.message}", e)
            }
        }
    }

    /**
     * Send custom notification
     */
    fun sendCustomNotification(
        title: String,
        message: String,
        type: NotificationType,
        priority: NotificationPriority,
        imageUrl: String? = null,
        actionUrl: String? = null
    ) {
        scope.launch {
            try {
                notificationHandler.handleGenericNotification(
                    title = title,
                    message = message,
                    type = type,
                    priority = priority,
                    imageUrl = imageUrl,
                    actionUrl = actionUrl
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error sending custom notification: ${e.message}", e)
            }
        }
    }

    // ============ Notification Management ============

    /**
     * Mark notification as read
     */
    fun markNotificationAsRead(notificationId: String) {
        scope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
                Log.d(TAG, "Notification marked as read: $notificationId")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read: ${e.message}", e)
            }
        }
    }

    /**
     * Mark all notifications as read
     */
    fun markAllNotificationsAsRead() {
        scope.launch {
            try {
                notificationRepository.markAllAsRead()
                Log.d(TAG, "All notifications marked as read")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking all notifications as read: ${e.message}", e)
            }
        }
    }

    /**
     * Delete notification
     */
    fun deleteNotification(notificationId: String) {
        scope.launch {
            try {
                notificationRepository.deleteNotificationById(notificationId)
                Log.d(TAG, "Notification deleted: $notificationId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting notification: ${e.message}", e)
            }
        }
    }

    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        scope.launch {
            try {
                notificationRepository.clearAllNotifications()
                Log.d(TAG, "All notifications cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing all notifications: ${e.message}", e)
            }
        }
    }

    /**
     * Delete old notifications (older than specified days)
     */
    fun deleteOldNotifications(daysOld: Int = 30) {
        scope.launch {
            try {
                notificationRepository.deleteOldNotifications(daysOld.toLong())
                Log.d(TAG, "Deleted notifications older than $daysOld days")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting old notifications: ${e.message}", e)
            }
        }
    }

    // ============ OneSignal Integration ============

    /**
     * Set external user ID (after login)
     */
    fun setExternalUserId(userId: String) {
        oneSignalManager.setExternalUserId(userId)
    }

    /**
     * Clear external user ID (on logout)
     */
    fun clearExternalUserId() {
        oneSignalManager.clearExternalUserId()
    }

    /**
     * Get OneSignal subscription ID
     */
    fun getSubscriptionId(): String? {
        return oneSignalManager.getSubscriptionId()
    }

    /**
     * Add user tags for segmentation
     */
    fun addUserTags(tags: Map<String, String>) {
        oneSignalManager.addTags(tags)
    }

    /**
     * Remove user tags
     */
    fun removeUserTags(tagKeys: List<String>) {
        oneSignalManager.removeTags(tagKeys)
    }

    /**
     * Control notification subscription
     */
    fun setNotificationSubscription(subscribed: Boolean) {
        oneSignalManager.setNotificationSubscription(subscribed)
    }
}
