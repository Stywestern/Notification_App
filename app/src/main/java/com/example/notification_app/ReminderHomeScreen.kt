package com.example.notification_app

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun ReminderHomeScreen() {
    val context = LocalContext.current
    val scheduler = remember { ReminderScheduler(context) }
    val storage = remember { ReminderStorage(context) }

    // Core Form States
    var selectedType by remember { mutableStateOf(ReminderType.DAILY_PERSISTENT) }
    var taskName by remember { mutableStateOf("") }

    // Selection Labels derived dynamically from PickerUtils calculations
    var selectedTimeText by remember { mutableStateOf("00:00:00") }
    var selectedDateTimeText by remember { mutableStateOf("Tap to select target date/time") }

    // Concrete temporary values to pass down to our data blueprint
    var targetTimestamp by remember { mutableLongStateOf(0L) }

    // Active Tracker List State
    var activeReminders by remember { mutableStateOf(storage.getReminders()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "", modifier = Modifier.padding(bottom = 12.dp))

        // 1. STRATEGY SELECTOR TABS
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Button(
                onClick = { selectedType = ReminderType.DAILY_PERSISTENT },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == ReminderType.DAILY_PERSISTENT) Color(0xFF333333) else Color(0xFFCCCCCC)
                )
            ) {
                Text("Daily Task", color = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { selectedType = ReminderType.EVENT_TIERED },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == ReminderType.EVENT_TIERED) Color(0xFF333333) else Color(0xFFCCCCCC)
                )
            ) {
                Text("Special Event", color = Color.White)
            }
        }

        // 2. NAME INPUT
        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task/Event Title") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        // 3. DYNAMIC NATIVE INTERFACES
        if (selectedType == ReminderType.DAILY_PERSISTENT) {
            Text("Set Repeat Interval Duration:", modifier = Modifier.padding(bottom = 4.dp))
            OutlinedButton(
                onClick = {
                    PickerUtils.showSlidingDurationPicker(context, "Select Loop Interval") { hours, minutes, seconds ->
                        selectedTimeText = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("Interval: $selectedTimeText")
            }
        } else {
            Text("Target Deadline:", modifier = Modifier.padding(bottom = 4.dp))
            OutlinedButton(
                onClick = {
                    PickerUtils.showDateTimePicker(context) { timestamp, formattedString ->
                        targetTimestamp = timestamp
                        selectedDateTimeText = "Scheduled: $formattedString"
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(selectedDateTimeText)
            }
        }

        // 4. SAVE PIPELINE ORCHESTRATION
        Button(
            onClick = {
                if (taskName.isNotBlank()) {
                    val newReminder = ReminderModel(
                        id = UUID.randomUUID().toString(),
                        title = taskName,
                        type = selectedType,
                        specificMoments = if (selectedType == ReminderType.DAILY_PERSISTENT) listOf(selectedTimeText) else null,
                        targetTimestamp = if (selectedType == ReminderType.EVENT_TIERED) targetTimestamp else null,
                        warningIntervalHours = if (selectedType == ReminderType.EVENT_TIERED) listOf(24, 1) else null
                    )

                    scheduler.schedule(newReminder)
                    activeReminders = activeReminders + newReminder
                    storage.saveReminders(activeReminders)

                    Toast.makeText(context, "Reminder Saved", Toast.LENGTH_SHORT).show()
                    taskName = ""
                    selectedTimeText = "00:00:00" // Properly zeroing out the state here
                    selectedDateTimeText = "Tap to select target date/time"
                } else {
                    Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Text("Save Reminder")
        }

        // Contextual Dashboard Title
        val dashboardTitle = if (selectedType == ReminderType.DAILY_PERSISTENT) "Active Daily Tasks" else "Active Special Events"
        Text(text = dashboardTitle, modifier = Modifier.padding(bottom = 8.dp))

        // 5. REACTIVE AND FILTERED LIST RENDERER
        Column(modifier = Modifier.fillMaxWidth()) {

            // Filter the memory list to only show items matching the current top tab
            val displayedReminders = activeReminders.filter { it.type == selectedType }

            if (displayedReminders.isEmpty()) {
                Text("No reminders in this category.", color = Color.Gray, modifier = Modifier.padding(8.dp))
            } else {
                displayedReminders.forEach { reminder ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = reminder.title)

                                // Dynamic Subtitle Logic
                                val subtitleText = if (reminder.type == ReminderType.DAILY_PERSISTENT) {
                                    "Looping every: ${reminder.specificMoments?.firstOrNull() ?: "Unknown"}"
                                } else {
                                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    val dateStr = reminder.targetTimestamp?.let { sdf.format(Date(it)) } ?: "Unknown Date"
                                    "Target: $dateStr"
                                }

                                Text(
                                    text = subtitleText,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // CLEAN TRASH CAN ICON
                            IconButton(
                                onClick = {
                                    scheduler.cancel(reminder.id)
                                    activeReminders = activeReminders.filter { it.id != reminder.id }
                                    storage.saveReminders(activeReminders)

                                    Toast.makeText(context, "Reminder removed", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Reminder",
                                    tint = Color(0xFFBB2222) // Keeps the destructive action visually distinct
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}