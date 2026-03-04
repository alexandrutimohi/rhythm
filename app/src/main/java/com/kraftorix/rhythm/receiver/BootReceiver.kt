package com.kraftorix.rhythm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kraftorix.rhythm.data.repository.PillAlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: PillAlarmRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Device boot completed. Rescheduling alarms.")
            val scheduler = AlarmScheduler(context)
            scope.launch {
                val alarms = repository.getAllAlarms().first()
                val currentTime = System.currentTimeMillis()
                alarms.forEach { alarm ->
                    if (alarm.isEnabled) {
                        val nextTrigger = calculateNextTrigger(alarm.startTime, alarm.intervalMillis)
                        Timber.d("Rescheduling ID=${alarm.id}, Name=${alarm.name}, NextTrigger=$nextTrigger")
                        scheduler.schedule(alarm.id, alarm.name, nextTrigger)
                    }
                }
            }
        }
    }

    private fun calculateNextTrigger(startTime: Long, intervalMillis: Long): Long {
        val currentTime = System.currentTimeMillis()
        if (startTime > currentTime) return startTime
        
        var nextTrigger = startTime
        if (intervalMillis <= 0) return startTime
        
        while (nextTrigger <= currentTime) {
            nextTrigger += intervalMillis
        }
        return nextTrigger
    }
}
