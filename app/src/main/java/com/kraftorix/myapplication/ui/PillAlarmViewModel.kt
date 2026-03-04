package com.kraftorix.myapplication.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kraftorix.myapplication.data.local.PillAlarmEntity
import com.kraftorix.myapplication.data.repository.PillAlarmRepository
import com.kraftorix.myapplication.receiver.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PillAlarmViewModel @Inject constructor(
    private val repository: PillAlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val alarms: StateFlow<List<PillAlarmEntity>> = repository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(name: String, startTime: Long, intervalMillis: Long, isFullScreenAlarm: Boolean) {
        viewModelScope.launch {
            val safeInterval = if (intervalMillis <= 0) 60000L else intervalMillis
            val nextTrigger = calculateNextTrigger(startTime, safeInterval)
            
            Log.d("PillReminderDebug", "ViewModel: Adding alarm. Name=$name, OriginalStartTime=$startTime, NextTrigger=$nextTrigger, Interval=$safeInterval")
            
            val alarm = PillAlarmEntity(
                name = name,
                startTime = startTime,
                intervalMillis = safeInterval,
                isEnabled = true,
                isFullScreenAlarm = isFullScreenAlarm
            )
            val id = repository.insertAlarm(alarm)
            alarmScheduler.schedule(id, name, nextTrigger)
        }
    }

    fun updateAlarm(alarm: PillAlarmEntity) {
        viewModelScope.launch {
            val safeInterval = if (alarm.intervalMillis <= 0) 60000L else alarm.intervalMillis
            val updatedAlarm = alarm.copy(intervalMillis = safeInterval)
            
            Log.d("PillReminderDebug", "ViewModel: Updating alarm ID=${alarm.id}. Name=${alarm.name}, Enabled=${alarm.isEnabled}")
            
            repository.updateAlarm(updatedAlarm)
            
            if (updatedAlarm.isEnabled) {
                val nextTrigger = calculateNextTrigger(updatedAlarm.startTime, safeInterval)
                alarmScheduler.schedule(updatedAlarm.id, updatedAlarm.name, nextTrigger)
            } else {
                alarmScheduler.cancel(updatedAlarm.id)
            }
        }
    }

    private fun calculateNextTrigger(startTime: Long, intervalMillis: Long): Long {
        val currentTime = System.currentTimeMillis()
        if (startTime > currentTime) return startTime
        
        var nextTrigger = startTime
        // Increment by interval until we find the next future occurrence
        while (nextTrigger <= currentTime) {
            nextTrigger += intervalMillis
        }
        return nextTrigger
    }

    fun deleteAlarm(alarm: PillAlarmEntity) {
        viewModelScope.launch {
            Log.d("PillReminderDebug", "ViewModel: Deleting alarm ID=${alarm.id}")
            repository.deleteAlarm(alarm)
            alarmScheduler.cancel(alarm.id)
        }
    }

    fun toggleAlarm(alarm: PillAlarmEntity) {
        val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
        Log.d("PillReminderDebug", "ViewModel: Toggling alarm ID=${alarm.id}. NewState=${updatedAlarm.isEnabled}")
        updateAlarm(updatedAlarm)
    }
}
