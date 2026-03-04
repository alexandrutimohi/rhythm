package com.kraftorix.rhythm.data.repository

import com.kraftorix.rhythm.data.local.PillAlarmDao
import com.kraftorix.rhythm.data.local.PillAlarmEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PillAlarmRepositoryImpl @Inject constructor(
    private val pillAlarmDao: PillAlarmDao
) : PillAlarmRepository {
    override fun getAllAlarms(): Flow<List<PillAlarmEntity>> = pillAlarmDao.getAllAlarms()

    override suspend fun getAlarmById(id: Long): PillAlarmEntity? = pillAlarmDao.getAlarmById(id)

    override suspend fun insertAlarm(alarm: PillAlarmEntity): Long = pillAlarmDao.insertAlarm(alarm)

    override suspend fun updateAlarm(alarm: PillAlarmEntity) = pillAlarmDao.updateAlarm(alarm)

    override suspend fun deleteAlarm(alarm: PillAlarmEntity) = pillAlarmDao.deleteAlarm(alarm)
}
