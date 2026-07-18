package com.deep.lumoraai.di

import android.content.Context
import androidx.room.Room
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.local.room.dao.HistoryDao
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
        return Room.databaseBuilder(
            context,
            LumoraDatabase::class.java,
            "lumora_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: LumoraDatabase): HistoryDao {
        return database.historyDao
    }
}
