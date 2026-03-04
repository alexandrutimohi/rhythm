package com.kraftorix.rhythm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kraftorix.rhythm.ui.PillAlarmViewModel
import com.kraftorix.rhythm.ui.theme.MyRhythmTheme
import java.util.*

@Composable
fun AddEditAlarmScreen(
    viewModel: PillAlarmViewModel,
    alarmId: Long?,
    onNavigateBack: () -> Unit
) {
    val alarms by viewModel.alarms.collectAsState()
    val existingAlarm = remember(alarmId, alarms) {
        alarms.find { it.id == alarmId }
    }

    AddEditAlarmContent(
        isEditMode = alarmId != null,
        initialName = existingAlarm?.name ?: "",
        initialIntervalHours = existingAlarm?.let { (it.intervalMillis / (60 * 60 * 1000)).toString() } ?: "8",
        initialIsFullScreenAlarm = existingAlarm?.isFullScreenAlarm ?: true,
        initialStartTime = existingAlarm?.startTime ?: System.currentTimeMillis(),
        onSave = { name, intervalHours, isFullScreen, startTime ->
            val intervalMillis = (intervalHours.toLongOrNull() ?: 8L) * 60 * 60 * 1000
            if (existingAlarm == null) {
                viewModel.addAlarm(name, startTime, intervalMillis, isFullScreen)
            } else {
                viewModel.updateAlarm(existingAlarm.copy(
                    name = name,
                    startTime = startTime,
                    intervalMillis = intervalMillis,
                    isFullScreenAlarm = isFullScreen
                ))
            }
            onNavigateBack()
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmContent(
    isEditMode: Boolean,
    initialName: String,
    initialIntervalHours: String,
    initialIsFullScreenAlarm: Boolean,
    initialStartTime: Long,
    onSave: (String, String, Boolean, Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var intervalHours by remember { mutableStateOf(initialIntervalHours) }
    var isFullScreenAlarm by remember { mutableStateOf(initialIsFullScreenAlarm) }
    
    val calendar = remember { 
        Calendar.getInstance().apply { timeInMillis = initialStartTime }
    }
    
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (!isEditMode) "New Reminder" else "Edit Reminder", 
                        fontWeight = FontWeight.ExtraBold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cal.set(Calendar.MINUTE, timePickerState.minute)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            
                            onSave(name, intervalHours, isFullScreenAlarm, cal.timeInMillis)
                        },
                        enabled = name.isNotBlank() && intervalHours.isNotBlank()
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Reminder for") },
                            placeholder = { Text("e.g. Water") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = intervalHours,
                            onValueChange = { if (it.all { char -> char.isDigit() }) intervalHours = it },
                            label = { Text("Every") },
                            suffix = { Text("h") },
                            modifier = Modifier.width(90.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Full Alarm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Pop up on screen with sound",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isFullScreenAlarm,
                        onCheckedChange = { isFullScreenAlarm = it }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(), 
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Start Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = timePickerState)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddAlarmPreview() {
    MyRhythmTheme {
        AddEditAlarmContent(
            isEditMode = false,
            initialName = "",
            initialIntervalHours = "8",
            initialIsFullScreenAlarm = true,
            initialStartTime = System.currentTimeMillis(),
            onSave = { _, _, _, _ -> },
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditAlarmPreview() {
    MyRhythmTheme {
        AddEditAlarmContent(
            isEditMode = true,
            initialName = "Aspirin",
            initialIntervalHours = "12",
            initialIsFullScreenAlarm = false,
            initialStartTime = System.currentTimeMillis(),
            onSave = { _, _, _, _ -> },
            onNavigateBack = {}
        )
    }
}
