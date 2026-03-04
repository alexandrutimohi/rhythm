package com.kraftorix.myapplication.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PillAlarmDao {
    @Query("SELECT * FROM pill_alarms ORDER BY startTime ASC")
    fun getAllAlarms(): Flow<List<PillAlarmEntity>>

    @Query("SELECT * FROM pill_alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): PillAlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: PillAlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: PillAlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: PillAlarmEntity)
}
