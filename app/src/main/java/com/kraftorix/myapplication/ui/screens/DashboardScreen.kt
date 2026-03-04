package com.kraftorix.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kraftorix.myapplication.data.local.PillAlarmEntity
import com.kraftorix.myapplication.ui.PillAlarmViewModel
import com.kraftorix.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: PillAlarmViewModel,
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit
) {
    val alarms by viewModel.alarms.collectAsState()
    DashboardContent(
        alarms = alarms,
        onAddAlarm = onAddAlarm,
        onEditAlarm = onEditAlarm,
        onToggle = { viewModel.toggleAlarm(it) },
        onDelete = { viewModel.deleteAlarm(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    alarms: List<PillAlarmEntity>,
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onToggle: (PillAlarmEntity) -> Unit,
    onDelete: (PillAlarmEntity) -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val nextReminderTime = remember(alarms) {
        alarms.filter { it.isEnabled }
            .map { calculateNextOccurrence(it.startTime, it.intervalMillis) }
            .minOrNull()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column(modifier = Modifier.padding(end = 16.dp)) {
                        Text(
                            "Rhythm", 
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold 
                        )
                        if (alarms.isNotEmpty()) {
                            val statusText = if (nextReminderTime != null) {
                                "Next at ${timeFormat.format(Date(nextReminderTime))}"
                            } else {
                                "All reminders paused"
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAlarm,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm", modifier = Modifier.size(28.dp))
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        if (alarms.isEmpty()) {
            EmptyDashboard(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alarms) { alarm ->
                    AlarmItem(
                        alarm = alarm,
                        onToggle = { onToggle(alarm) },
                        onDelete = { onDelete(alarm) },
                        onClick = { onEditAlarm(alarm.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDashboard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.NotificationsOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Clear schedule",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Tap + to set your first reminder",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmItem(
    alarm: PillAlarmEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    // Calculate next dose time
    val nextDoseMillis = remember(alarm.startTime, alarm.intervalMillis) {
        calculateNextOccurrence(alarm.startTime, alarm.intervalMillis)
    }
    val nextDoseStr = timeFormat.format(Date(nextDoseMillis))
    
    val frequencyText = remember(alarm.intervalMillis) {
        when (alarm.intervalMillis) {
            24 * 60 * 60 * 1000L -> "Once daily"
            12 * 60 * 60 * 1000L -> "Twice daily"
            8 * 60 * 60 * 1000L -> "3x daily"
            6 * 60 * 60 * 1000L -> "4x daily"
            else -> "Every ${alarm.intervalMillis / (60 * 60 * 1000)}h"
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (alarm.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (alarm.isEnabled) {
                            if (alarm.isFullScreenAlarm) Icons.Default.VolumeUp else Icons.Default.Notifications
                        } else {
                            Icons.Default.NotificationsOff
                        },
                        contentDescription = null,
                        tint = if (alarm.isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (alarm.isEnabled) "Next: $nextDoseStr" else "Paused",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • $frequencyText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Alarm Type Label
                Text(
                    text = if (alarm.isFullScreenAlarm) "Full Alarm" else "Notification Only",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun calculateNextOccurrence(startTime: Long, intervalMillis: Long): Long {
    val currentTime = System.currentTimeMillis()
    if (startTime > currentTime) return startTime
    
    val elapsed = currentTime - startTime
    val occurrences = (elapsed / intervalMillis) + 1
    return startTime + (occurrences * intervalMillis)
}

@Preview(showBackground = true)
@Composable
fun DashboardLightPreview() {
    MyApplicationTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            DashboardContent(
                alarms = listOf(
                    PillAlarmEntity(1, "Hydration", 1713156960000, 3600000, true, isFullScreenAlarm = false),
                    PillAlarmEntity(2, "Stretching", 1713158760000, 86400000, false, isFullScreenAlarm = true),
                    PillAlarmEntity(3, "Vitamins", 1713160560000, 43200000, true, isFullScreenAlarm = true)
                ),
                onAddAlarm = {},
                onEditAlarm = {},
                onToggle = {},
                onDelete = {}
            )
        }
    }
}
