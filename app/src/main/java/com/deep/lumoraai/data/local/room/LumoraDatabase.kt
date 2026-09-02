package com.deep.lumoraai.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
                ).addMigrations(MIGRATION_1_3, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }

        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createNotificationTable(db)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createNotificationTable(db)
            }
        }

        private fun createNotificationTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notification_table` (
                    `id` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `message` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `priority` TEXT NOT NULL,
                    `imageUrl` TEXT,
                    `actionUrl` TEXT,
                    `isRead` INTEGER NOT NULL DEFAULT 0,
                    `createdAt` INTEGER NOT NULL,
                    `oneSignalId` TEXT,
                    `taskId` TEXT,
                    `resultId` TEXT,
                    `taskType` TEXT,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }
}
