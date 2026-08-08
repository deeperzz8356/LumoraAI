package com.deep.lumoraai.data.mapper

import com.deep.lumoraai.data.local.room.entity.HistoryEntity
import com.deep.lumoraai.data.model.HistoryModel

fun HistoryEntity.toDomainModel(): HistoryModel {
    return HistoryModel(
        id = id,
        title = title,
        createdAt = createdAt,
        type = type,
        mediaUrl = mediaUrl,
    )
}

fun HistoryModel.toEntity(type: String, mediaUrl: String? = null): HistoryEntity {
    return HistoryEntity(
        id = id,
        title = title,
        type = type,
        mediaUrl = mediaUrl,
        createdAt = createdAt
    )
}
