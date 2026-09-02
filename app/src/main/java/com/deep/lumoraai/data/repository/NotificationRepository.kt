package com.deep.lumoraai.data.repository

import com.deep.lumoraai.data.local.room.dao.NotificationDao
import com.deep.lumoraai.data.mapper.toDomain
import com.deep.lumoraai.data.mapper.toEntity
import com.deep.lumoraai.domain.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    
    suspend fun insertNotification(notification: Notification) {
        notificationDao.insertNotification(notification.toEntity())
    }
    
    suspend fun insertNotifications(notifications: List<Notification>) {
        notificationDao.insertNotifications(notifications.map { it.toEntity() })
    }
    
    suspend fun updateNotification(notification: Notification) {
        notificationDao.updateNotification(notification.toEntity())
    }
    
    suspend fun deleteNotification(notification: Notification) {
        notificationDao.deleteNotification(notification.toEntity())
    }
    
    suspend fun deleteNotificationById(id: String) {
        notificationDao.deleteNotificationById(id)
    }
    
    suspend fun getNotificationById(id: String): Notification? {
        return notificationDao.getNotificationById(id)?.toDomain()
    }
    
    fun getAllNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications().map { it.toDomain() }
    }
    
    fun getUnreadNotifications(): Flow<List<Notification>> {
        return notificationDao.getUnreadNotifications().map { it.toDomain() }
    }
    
    fun getNotificationsByType(type: String): Flow<List<Notification>> {
        return notificationDao.getNotificationsByType(type).map { it.toDomain() }
    }
    
    fun getNotificationsByPriority(priority: String): Flow<List<Notification>> {
        return notificationDao.getNotificationsByPriority(priority).map { it.toDomain() }
    }
    
    fun getUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }
    
    suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }
    
    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }
    
    suspend fun clearAllNotifications() {
        notificationDao.clearAllNotifications()
    }
    
    suspend fun deleteOldNotifications(timestamp: Long) {
        notificationDao.deleteOldNotifications(timestamp)
    }
}
