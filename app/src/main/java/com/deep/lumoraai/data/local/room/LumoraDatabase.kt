package com.deep.lumoraai.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deep.lumoraai.data.local.room.dao.HistoryDao
import com.deep.lumoraai.data.local.room.dao.NotificationDao
import com.deep.lumoraai.data.local.room.entity.HistoryEntity
import com.deep.lumoraai.data.local.room.entity.NotificationEntity

@Database(
    entities = [HistoryEntity::class, NotificationEntity::class],
    version = 3,
    exportSchema = false
)
abstract class LumoraDatabase : RoomDatabase() {
    abstract val historyDao: HistoryDao
    abstract val notificationDao: NotificationDao

    companion object {
        @Volatile
        private var instance: LumoraDatabase? = null

        fun getInstance(context: Context): LumoraDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LumoraDatabase::class.java,
                    "lumora_database"
                ).build().also { instance = it }
            }
    }
}
