package com.deep.lumoraai.core.notification

import android.util.Log
import com.deep.lumoraai.data.local.room.dao.NotificationDao
import com.deep.lumoraai.data.local.room.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages local notifications stored in Room database
 * Handles creation, reading, and deletion of notifications
 */
@Singleton
class NotificationManager @Inject constructor(
    private val notificationDao: NotificationDao
) {
    companion object {
        private const val TAG = "NotificationManager"
    }

    // Task start notification
    suspend fun sendTaskStartNotification(
        taskType: String,
        taskId: String,
        displayName: String = taskType
    ) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = "Processing Started",
            message = "$displayName generation has begun",
            type = "TASK_PROGRESS",
            priority = "MEDIUM",
            imageUrl = null,
            actionUrl = null,
            isRead = false,
            createdAt = System.currentTimeMillis(),
            taskId = taskId,
            taskType = taskType
        )
        notificationDao.insertNotification(notification)
        Log.d(TAG, "Task start notification sent: $taskType")
    }

    // Task complete notification
    suspend fun sendTaskCompleteNotification(
        taskType: String,
        taskId: String,
        resultId: String,
        displayName: String = taskType,
        message: String = "Your creation is ready to view or download",
        thumbnailUrl: String? = null
    ) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = "✓ $displayName Complete",
            message = message,
            type = "TASK_COMPLETION",
            priority = "HIGH",
            imageUrl = thumbnailUrl,
            actionUrl = "com.deep.lumoraai://result?resultId=$resultId",
            isRead = false,
            createdAt = System.currentTimeMillis(),
            taskId = taskId,
            resultId = resultId,
            taskType = taskType
        )
        notificationDao.insertNotification(notification)
        Log.d(TAG, "Task complete notification sent: $taskType")
    }

    // Task failure notification
    suspend fun sendTaskFailureNotification(
        taskType: String,
        taskId: String,
        displayName: String = taskType,
        errorMessage: String = "Error processing task"
    ) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = "⚠️ $displayName Failed",
            message = errorMessage,
            type = "ERROR",
            priority = "HIGH",
            imageUrl = null,
            actionUrl = null,
            isRead = false,
            createdAt = System.currentTimeMillis(),
            taskId = taskId,
            taskType = taskType
        )
        notificationDao.insertNotification(notification)
        Log.d(TAG, "Task failure notification sent: $taskType")
    }

    // Get notifications by task ID
    fun getNotificationsByTaskId(taskId: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByTaskId(taskId)
    }

    // Get notifications by result ID
    fun getNotificationsByResultId(resultId: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByResultId(resultId)
    }

    // Get notifications by task type
    fun getNotificationsByTaskType(taskType: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByTaskType(taskType)
    }

    // Task completion notification
    suspend fun sendTaskCompletionNotification(
        title: String,
        message: String,
        actionUrl: String? = null,
        imageUrl: String? = null
    ) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = "TASK_COMPLETION",
            priority = "HIGH",
            imageUrl = imageUrl,
            actionUrl = actionUrl,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notification)
        Log.d(TAG, "Task completion notification sent: $title")
    }

    // Engagement notification (reminders, alerts)
    suspend fun sendEngagementNotification(
        title: String,
        message: String,
        actionUrl: String? = null,
        imageUrl: String? = null
    ) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = "ENGAGEMENT",
            priority = "MEDIUM",
            imageUrl = imageUrl,
            actionUrl = actionUrl,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notification)
        Log.d(TAG, "Engagement notification sent: $title")
    }

    // Feature announcement
    suspend fun sendFeatureAnnouncement(
        title: String,
        message: String,
        actionUrl: String? = null,
        imageUrl: String? = null
    ) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = "FEATURE_ANNOUNCEMENT",
            priority = "MEDIUM",
            imageUrl = imageUrl,
            actionUrl = actionUrl,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notification)
        Log.d(TAG, "Feature announcement sent: $title")
    }

    // Error notification
    suspend fun sendErrorNotification(
        title: String,
        message: String,
        actionUrl: String? = null
    ) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = "ERROR",
            priority = "HIGH",
            actionUrl = actionUrl,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notification)
        Log.d(TAG, "Error notification sent: $title")
    }

    // Custom notification with full control
    suspend fun sendCustomNotification(
        title: String,
        message: String,
        type: String = "CUSTOM",
        priority: String = "MEDIUM",
        actionUrl: String? = null,
        imageUrl: String? = null
    ) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = type,
            priority = priority,
            imageUrl = imageUrl,
            actionUrl = actionUrl,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notification)
        Log.d(TAG, "Custom notification sent: $title")
    }

    // Get all notifications as Flow
    fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAllNotifications()
    }

    // Get only unread notifications
    fun getUnreadNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getUnreadNotifications()
    }

    // Get notifications by type
    fun getNotificationsByType(type: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByType(type)
    }

    // Get notifications by priority
    fun getNotificationsByPriority(priority: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByPriority(priority)
    }

    // Get unread count
    fun getUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }

    // Mark notification as read
    suspend fun markAsRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
        Log.d(TAG, "Notification marked as read: $notificationId")
    }

    // Mark all as read
    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
        Log.d(TAG, "All notifications marked as read")
    }

    // Delete notification
    suspend fun deleteNotification(notificationId: String) {
        notificationDao.deleteNotificationById(notificationId)
        Log.d(TAG, "Notification deleted: $notificationId")
    }

    // Clear all notifications
    suspend fun clearAllNotifications() {
        notificationDao.clearAllNotifications()
        Log.d(TAG, "All notifications cleared")
    }

    // Delete notifications older than 30 days
    suspend fun deleteOldNotifications() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
        notificationDao.deleteOldNotifications(thirtyDaysAgo)
        Log.d(TAG, "Old notifications deleted (older than 30 days)")
    }

    // Get a single notification by ID
    suspend fun getNotificationById(id: String): NotificationEntity? {
        return notificationDao.getNotificationById(id)
    }
}
