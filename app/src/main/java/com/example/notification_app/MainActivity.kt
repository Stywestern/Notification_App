package com.example.notification_app

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dynamic permission request for Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        setContent {
            ReminderHomeScreen()
        }
    }
}

@Composable
fun ReminderHomeScreen() {
    // Fetch the active Android Context inside a Composable structure
    val context = LocalContext.current

    // Initialize our modules using the current context context
    val scheduler = remember { ReminderScheduler(context) }

    var taskName by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Create a New Reminder", modifier = Modifier.padding(bottom = 8.dp))

        OutlinedTextField(
            value = taskName,
            onValueChange = { newValue -> taskName = newValue },
            label = { Text("Task Name") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = {
            if (taskName.isNotBlank()) {
                // Calculate an exact timestamp 10 seconds from now
                val testExecutionTime = System.currentTimeMillis() + 10000L

                // Assemble a mock one-off Event model payload
                val mockReminder = ReminderModel(
                    id = UUID.randomUUID().toString(),
                    title = taskName,
                    type = ReminderType.EVENT_TIERED,
                    targetTimestamp = testExecutionTime
                )

                // Dispatch the instructions to our background scheduler orchestrator
                scheduler.schedule(mockReminder)

                // Give immediate visual feedback to the user
                Toast.makeText(context, "Alarm set for 10 seconds!", Toast.LENGTH_SHORT).show()
                taskName = "" // Reset field
            } else {
                Toast.makeText(context, "Please enter a task name first", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Save Reminder")
        }
    }
}