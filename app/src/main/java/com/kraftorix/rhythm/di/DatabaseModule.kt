package com.kraftorix.rhythm.di

import android.content.Context
import androidx.room.Room
import com.kraftorix.rhythm.data.local.PillAlarmDao
import com.kraftorix.rhythm.data.local.PillDatabase
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
    fun providePillDatabase(@ApplicationContext context: Context): PillDatabase {
        return Room.databaseBuilder(
            context,
            PillDatabase::class.java,
            "pill_reminder_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePillAlarmDao(database: PillDatabase): PillAlarmDao {
        return database.pillAlarmDao()
    }
}
