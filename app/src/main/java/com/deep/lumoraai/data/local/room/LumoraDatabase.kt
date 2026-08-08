package com.deep.lumoraai.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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
