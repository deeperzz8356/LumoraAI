package com.deep.lumoraai.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.deep.lumoraai.data.local.room.dao.HistoryDao
import com.deep.lumoraai.data.local.room.entity.HistoryEntity

@Database(
    entities = [HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LumoraDatabase : RoomDatabase() {
    abstract val historyDao: HistoryDao
}
