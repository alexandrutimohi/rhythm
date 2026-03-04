package com.kraftorix.myapplication.di

import com.kraftorix.myapplication.data.repository.PillAlarmRepository
import com.kraftorix.myapplication.data.repository.PillAlarmRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPillAlarmRepository(
        pillAlarmRepositoryImpl: PillAlarmRepositoryImpl
    ): PillAlarmRepository
}
