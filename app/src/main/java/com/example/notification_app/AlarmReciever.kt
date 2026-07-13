package com.example.notification_app

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Unpack the data payload sent by our Scheduler
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: return
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Reminder"
        val intervalMillis = intent.getLongExtra("INTERVAL_MILLIS", 0L)

        // 2. Fire the visual notification to the user
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // (Assuming you have your "REMINDER_CHANNEL" created in MainActivity)
        val notification = NotificationCompat.Builder(context, "REMINDER_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Task Triggered!")
            .setContentText(reminderTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Use the hashcode as the notification ID so different tasks don't overwrite each other in the tray
        notificationManager.notify(reminderId.hashCode(), notification)

        // 3. THE DAISY-CHAIN LOOP: Reschedule the next hop automatically
        if (intervalMillis > 0) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextTriggerTime = System.currentTimeMillis() + intervalMillis

            // We must clone the exact same intent structure to maintain the system loop
            val nextIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("REMINDER_ID", reminderId)
                putExtra("REMINDER_TITLE", reminderTitle)
                putExtra("REMINDER_TAG", "INTERVAL_LOOP")
                putExtra("INTERVAL_MILLIS", intervalMillis)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Throw the boomerang back to the OS for the next cycle
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        }
    }
}