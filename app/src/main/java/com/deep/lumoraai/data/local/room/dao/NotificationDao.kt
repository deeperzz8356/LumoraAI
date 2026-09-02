package com.deep.lumoraai.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.deep.lumoraai.data.local.room.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    
    @Insert
    suspend fun insertNotification(notification: NotificationEntity)
    
    @Insert
    suspend fun insertNotifications(notifications: List<NotificationEntity>)
    
    @Update
    suspend fun updateNotification(notification: NotificationEntity)
    
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
    
    @Query("DELETE FROM notification_table WHERE id = :id")
    suspend fun deleteNotificationById(id: String)
    
    @Query("SELECT * FROM notification_table WHERE id = :id")
    suspend fun getNotificationById(id: String): NotificationEntity?
    
    @Query("SELECT * FROM notification_table ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notification_table WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadNotifications(): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notification_table WHERE type = :type ORDER BY createdAt DESC")
    fun getNotificationsByType(type: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notification_table WHERE priority = :priority ORDER BY createdAt DESC")
    fun getNotificationsByPriority(priority: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notification_table WHERE taskId = :taskId ORDER BY createdAt DESC")
    fun getNotificationsByTaskId(taskId: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notification_table WHERE resultId = :resultId ORDER BY createdAt DESC")
    fun getNotificationsByResultId(resultId: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notification_table WHERE taskType = :taskType ORDER BY createdAt DESC")
    fun getNotificationsByTaskType(taskType: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT COUNT(*) FROM notification_table WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
    
    @Query("UPDATE notification_table SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
    
    @Query("UPDATE notification_table SET isRead = 1")
    suspend fun markAllAsRead()
    
    @Query("DELETE FROM notification_table")
    suspend fun clearAllNotifications()
    
    @Query("DELETE FROM notification_table WHERE createdAt < :timestamp")
    suspend fun deleteOldNotifications(timestamp: Long)
}
