package com.example.notification_app

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun ReminderHomeScreen() {
    val context = LocalContext.current
    val scheduler = remember { ReminderScheduler(context) }

    // Core Form States
    var selectedType by remember { mutableStateOf(ReminderType.DAILY_PERSISTENT) }
    var taskName by remember { mutableStateOf("") }

    // Selection Labels derived dynamically from PickerUtils calculations
    var selectedTimeText by remember { mutableStateOf("00:00:00") }
    var selectedDateTimeText by remember { mutableStateOf("Tap to select target date/time") }

    // Concrete temporary values to pass down to our data blueprint
    var targetTimestamp by remember { mutableLongStateOf(0L) }

    // Active Tracker List State (Our front-end dashboard monitor memory)
    var activeReminders by remember { mutableStateOf(emptyList<ReminderModel>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Custom Reminder Planner", modifier = Modifier.padding(bottom = 12.dp))

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
                        // Automatically handles auto-fill format rule seamlessly
                        selectedTimeText = String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
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
                        // Passing the relevant fields depending on choice selection
                        specificMoments = if (selectedType == ReminderType.DAILY_PERSISTENT) listOf(selectedTimeText) else null,
                        targetTimestamp = if (selectedType == ReminderType.EVENT_TIERED) targetTimestamp else null,
                        warningIntervalHours = if (selectedType == ReminderType.EVENT_TIERED) listOf(24, 1) else null
                    )

                    // Issue commands to background engine
                    scheduler.schedule(newReminder)

                    // Append item into dashboard interface list state dynamically
                    activeReminders = activeReminders + newReminder

                    // Reset form fields cleanly
                    Toast.makeText(context, "Blueprint Saved and Tracked!", Toast.LENGTH_SHORT).show()
                    taskName = ""
                    selectedTimeText = "00:00:00"
                    selectedDateTimeText = "Tap to select target date/time"
                } else {
                    Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Text("Save Blueprint")
        }

        Text(text = "Active Monitors Dashboard", modifier = Modifier.padding(bottom = 8.dp))

        // 5. REACTIVE LIST RENDERER
        Column(modifier = Modifier.fillMaxWidth()) {
            if (activeReminders.isEmpty()) {
                Text("No reminders scheduled yet.", color = Color.Gray, modifier = Modifier.padding(8.dp))
            } else {
                activeReminders.forEach { reminder ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                    ) {
                        // Use a horizontal Row container to align text and the action button split side-by-side
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = reminder.title)
                                Text(
                                    text = "Strategy: ${reminder.type.name}",
                                    color = Color.Gray,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                                )
                            }

                            // DELETE ACTION INTERFACE
                            Button(
                                onClick = {
                                    // 1. Tell the background orchestrator to cancel the alarm trigger
                                    scheduler.cancel(reminder.id)

                                    // 2. Filter out this object index target from our screen memory list state
                                    activeReminders = activeReminders.filter { it.id != reminder.id }

                                    android.widget.Toast.makeText(context, "Reminder removed", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB2222))
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}