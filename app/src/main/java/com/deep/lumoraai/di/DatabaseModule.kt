package com.deep.lumoraai.di

import android.content.Context
import androidx.room.Room
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.local.room.dao.HistoryDao
import com.deep.lumoraai.data.local.room.dao.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLumoraDatabase(@ApplicationContext context: Context): LumoraDatabase {
        return LumoraDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: LumoraDatabase): HistoryDao {
        return database.historyDao
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: LumoraDatabase): NotificationDao {
        return database.notificationDao
    }
}
