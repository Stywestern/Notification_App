package com.example.notification_app

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * The core Background Orchestrator module.
 * Translates our app's business logic into system-level OS instructions.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Orchestrates the scheduling strategy based on the Reminder type.
     */
    fun schedule(reminder: ReminderModel) {
        when (reminder.type) {
            ReminderType.DAILY_PERSISTENT -> scheduleDaily(reminder)
            ReminderType.EVENT_TIERED -> scheduleEventWithTiers(reminder)
        }
    }

    /**
     * Strategy for scheduling continuous, repeating tasks.
     */
    private fun scheduleDaily(reminder: ReminderModel) {
        val calendar = Calendar.getInstance().apply {
            // Defaulting to 9 AM today if specific moments aren't detailed yet
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If 9 AM has already passed today, roll it over to tomorrow
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("REMINDER_TITLE", reminder.title)
        }

        // Generate a unique system passport based on the reminder's hash code
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule it to repeat every 24 hours indefinitely
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    /**
     * Strategy for scheduling tiered, fading event warnings.
     */
    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleEventWithTiers(reminder: ReminderModel) {
        val mainTimestamp = reminder.targetTimestamp ?: return

        // 1. Schedule the absolute final event arrival notification
        scheduleSingleAlarm(reminder.id, reminder.title, mainTimestamp, "FINAL")

        // 2. Loop through and schedule each warning threshold
        reminder.warningIntervalHours?.forEach { hoursPrior ->
            val warningTimestamp = mainTimestamp - (hoursPrior * 60 * 60 * 1000L)

            // Only schedule if the warning moment is still in the future!
            if (warningTimestamp > System.currentTimeMillis()) {
                val uniqueId = "${reminder.id}_warn_$hoursPrior"
                val warningTitle = "Approaching: ${reminder.title} (in $hoursPrior hours)"
                scheduleSingleAlarm(uniqueId, warningTitle, warningTimestamp, "TIER")
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleSingleAlarm(id: String, title: String, triggerAtMillis: Long, tag: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", id)
            putExtra("REMINDER_TITLE", title)
            putExtra("REMINDER_TAG", tag)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Wake the device up exactly on the millisecond even if idle
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}