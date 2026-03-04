package com.kraftorix.rhythm.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kraftorix.rhythm.data.repository.PillAlarmRepository
import com.kraftorix.rhythm.ui.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: PillAlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        val alarmName = intent.getStringExtra(EXTRA_ALARM_NAME) ?: "Routine"
        val action = intent.action

        Timber.d("AlarmReceiver onReceive: ID=$alarmId, Action=$action, Name=$alarmName")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_SNOOZE -> {
                        Timber.d("Processing Snooze for ID=$alarmId")
                        snoozeAlarm(context, alarmId, alarmName)
                        dismissNotification(context, alarmId)
                    }
                    ACTION_DISMISS -> {
                        Timber.d("Processing Dismiss for ID=$alarmId")
                        handleDismiss(context, alarmId)
                    }
                    else -> {
                        val alarm = repository.getAlarmById(alarmId)
                        val isFullScreen = alarm?.isFullScreenAlarm ?: true
                        
                        Timber.d("Triggering reminder for ID=$alarmId, isFullScreen=$isFullScreen")
                        
                        // 1. Show notification
                        showNotification(context, alarmId, alarmName, isFullScreen)
                        
                        // 2. Also try to start activity directly if requested and screen is on
                        if (isFullScreen) {
                            startAlarmActivity(context, alarmId, alarmName)
                        }
                        
                        // 3. Schedule the next occurrence
                        if (alarm != null && alarm.isEnabled) {
                            scheduleNextOccurrence(context, alarmId, alarm.intervalMillis)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in AlarmReceiver")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun startAlarmActivity(context: Context, alarmId: Long, alarmName: String) {
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_NAME, alarmName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        try {
            context.startActivity(activityIntent)
        } catch (e: Exception) {
            Timber.e(e, "Could not start AlarmActivity directly")
        }
    }

    private fun showNotification(context: Context, alarmId: Long, alarmName: String, isFullScreen: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = if (isFullScreen) "rhythm_alarm_channel" else "rhythm_notification_channel"

        val name = if (isFullScreen) "Alarms" else "Reminders"
        val importance = if (isFullScreen) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = if (isFullScreen) "Urgent alarms" else "Regular reminders"
            enableVibration(true)
            if (isFullScreen) {
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
        }
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Rhythm")
            .setContentText("Time for: $alarmName")
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (isFullScreen) {
            val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_ALARM_NAME, alarmName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            }
            
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                alarmId.toInt(),
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            builder.setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setOngoing(true)
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
        }

        notificationManager.notify(alarmId.toInt(), builder.build())
    }

    private fun snoozeAlarm(context: Context, alarmId: Long, alarmName: String) {
        val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000 // 10 minutes
        val scheduler = AlarmScheduler(context)
        scheduler.schedule(alarmId, alarmName, snoozeTime)
    }

    private fun handleDismiss(context: Context, alarmId: Long) {
        dismissNotification(context, alarmId)
    }

    private fun scheduleNextOccurrence(context: Context, alarmId: Long, intervalMillis: Long) {
        val nextTrigger = System.currentTimeMillis() + intervalMillis
        val scheduler = AlarmScheduler(context)
        // We need to fetch the alarm name again or pass it in. 
        // For simplicity let's assume we have it or fetch it.
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = repository.getAlarmById(alarmId)
            if (alarm != null) {
                scheduler.schedule(alarm.id, alarm.name, nextTrigger)
            }
        }
    }

    private fun dismissNotification(context: Context, alarmId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(alarmId.toInt())
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_NAME = "extra_alarm_name"
        const val ACTION_SNOOZE = "com.kraftorix.rhythm.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.kraftorix.rhythm.ACTION_DISMISS"
    }
}
