package com.example.notification_app

import android.os.Bundle
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
import androidx.compose.ui.unit.dp

// Main layout
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderHomeScreen()
        }
    }
}

// Composable creates a block
@Composable
fun ReminderHomeScreen() {
    // This variable tracks what the user types into the text field
    var taskName by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Create a New Reminder", modifier = Modifier.padding(bottom = 8.dp))

        // The input box for the task
        OutlinedTextField(
            value = taskName,
            onValueChange = { newValue -> taskName = newValue },
            label = { Text("Task Name") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // The button to submit
        Button(onClick = {
            // We will hook this up to our background alarm engine later
        }) {
            Text(text = "Save Reminder")
        }
    }
}