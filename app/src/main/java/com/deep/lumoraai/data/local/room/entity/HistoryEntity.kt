package com.deep.lumoraai.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // "IMAGE" or "VIDEO"
    val mediaUrl: String?,
    val createdAt: String
)
