package com.kraftorix.myapplication.data.repository

import com.kraftorix.myapplication.data.local.PillAlarmEntity
import kotlinx.coroutines.flow.Flow

interface PillAlarmRepository {
    fun getAllAlarms(): Flow<List<PillAlarmEntity>>
    suspend fun getAlarmById(id: Long): PillAlarmEntity?
    suspend fun insertAlarm(alarm: PillAlarmEntity): Long
    suspend fun updateAlarm(alarm: PillAlarmEntity)
    suspend fun deleteAlarm(alarm: PillAlarmEntity)
}
