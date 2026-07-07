package com.example.notification_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * The Background Execution Module.
 * Awakened by the Android OS to catch fired alarms and post them to the screen.
 */
class AlarmReceiver : BroadcastReceiver() {

    private val CHANNEL_ID = "REMINDER_NOTIFICATIONS_CHANNEL"

    override fun onReceive(context: Context, intent: Intent) {
        // Extract the data string packages sent over by our ReminderScheduler orchestrator
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: "0"
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Task Reminder"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Ensure the system communication channel is initialized (Required for Android 8.0+)
        createNotificationChannel(notificationManager)

        // 2. Construct the visual notification layout
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using a built-in Android clock icon
            .setContentTitle("Reminder Alert")
            .setContentText(reminderTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Automatically dismisses the notification when tapped

        // 3. Post the notification onto the phone screen
        notificationManager.notify(reminderId.hashCode(), notificationBuilder.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        // Notification Channels are mandatory on Android 8.0 (API 26) and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channels alerts and warning triggers from your custom planner"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}