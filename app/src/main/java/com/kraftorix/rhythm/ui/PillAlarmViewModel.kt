package com.kraftorix.rhythm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kraftorix.rhythm.data.local.PillAlarmEntity
import com.kraftorix.rhythm.data.repository.PillAlarmRepository
import com.kraftorix.rhythm.receiver.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
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
            
            Timber.d("ViewModel: Adding alarm. Name=$name, OriginalStartTime=$startTime, NextTrigger=$nextTrigger, Interval=$safeInterval")
            
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
            
            Timber.d("ViewModel: Updating alarm ID=${alarm.id}. Name=${alarm.name}, Enabled=${alarm.isEnabled}")
            
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
            Timber.d("ViewModel: Deleting alarm ID=${alarm.id}")
            repository.deleteAlarm(alarm)
            alarmScheduler.cancel(alarm.id)
        }
    }

    fun toggleAlarm(alarm: PillAlarmEntity) {
        val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
        Timber.d("ViewModel: Toggling alarm ID=${alarm.id}. NewState=${updatedAlarm.isEnabled}")
        updateAlarm(updatedAlarm)
    }
}
