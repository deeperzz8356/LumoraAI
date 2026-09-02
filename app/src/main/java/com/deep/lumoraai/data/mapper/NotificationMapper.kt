package com.deep.lumoraai.data.mapper

import com.deep.lumoraai.data.local.room.entity.NotificationEntity
import com.deep.lumoraai.domain.model.Notification
import com.deep.lumoraai.domain.model.NotificationPriority
import com.deep.lumoraai.domain.model.NotificationType

fun NotificationEntity.toDomain(): Notification = Notification(
    id = id,
    title = title,
    message = message,
    type = NotificationType.valueOf(type),
    priority = NotificationPriority.valueOf(priority),
    imageUrl = imageUrl,
    actionUrl = actionUrl,
    isRead = isRead,
    createdAt = createdAt,
    oneSignalId = oneSignalId
)

fun Notification.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    title = title,
    message = message,
    type = type.name,
    priority = priority.name,
    imageUrl = imageUrl,
    actionUrl = actionUrl,
    isRead = isRead,
    createdAt = createdAt,
    oneSignalId = oneSignalId
)

fun List<NotificationEntity>.toDomain(): List<Notification> = map { it.toDomain() }
