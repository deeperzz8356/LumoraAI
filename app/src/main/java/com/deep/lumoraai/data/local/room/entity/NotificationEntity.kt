package com.deep.lumoraai.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_table")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String, // "TASK_COMPLETION", "ENGAGEMENT", "FEATURE_ANNOUNCEMENT", "TASK_PROGRESS", "ERROR"
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val imageUrl: String? = null,
    val actionUrl: String? = null, // Deep link URL
    val isRead: Boolean = false,
    val createdAt: Long, // Timestamp in milliseconds
    val oneSignalId: String? = null, // OneSignal notification ID for tracking
    val taskId: String? = null, // Reference to generation task
    val resultId: String? = null, // Reference to result/creation
    val taskType: String? = null // "TEXT_TO_IMAGE", "IMAGE_TO_VIDEO", etc.
)
