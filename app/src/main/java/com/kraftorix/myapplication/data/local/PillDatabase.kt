package com.kraftorix.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PillAlarmEntity::class], version = 2, exportSchema = false)
abstract class PillDatabase : RoomDatabase() {
    abstract fun pillAlarmDao(): PillAlarmDao
}
