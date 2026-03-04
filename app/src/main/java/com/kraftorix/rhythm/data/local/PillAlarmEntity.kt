package com.kraftorix.rhythm.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pill_alarms")
data class PillAlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startTime: Long,
    val intervalMillis: Long,
    val isEnabled: Boolean = true,
    val isFullScreenAlarm: Boolean = true
)
