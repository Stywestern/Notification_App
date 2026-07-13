package com.example.notification_app

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

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
            ReminderType.DAILY_PERSISTENT -> scheduleIntervalLoop(reminder)
            ReminderType.EVENT_TIERED -> scheduleEventWithTiers(reminder)
        }
    }

    /**
     * Strategy for scheduling continuous, relative-interval tasks.
     */
    private fun scheduleIntervalLoop(reminder: ReminderModel) {
        // 1. Extract the text string from our wheel selection (e.g., "00:03:00")
        val intervalString = reminder.specificMoments?.firstOrNull() ?: "00:00:10"

        // 2. Parse the string tokens into raw milliseconds
        val parts = intervalString.split(":")
        val hours = parts.getOrNull(0)?.toLongOrNull() ?: 0L
        val minutes = parts.getOrNull(1)?.toLongOrNull() ?: 0L
        val seconds = parts.getOrNull(2)?.toLongOrNull() ?: 0L

        var totalIntervalMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L

        // SAFETY FLOOR CRITICAL CHECK FOR TESTING:
        // If testing under 10 seconds, enforce at least 10 seconds so the OS doesn't kill it instantly
        if (totalIntervalMillis < 10000L) {
            totalIntervalMillis = 10000L
        }

        val triggerTime = System.currentTimeMillis() + totalIntervalMillis

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("REMINDER_TITLE", reminder.title)
            putExtra("REMINDER_TAG", "INTERVAL_LOOP")
            putExtra("INTERVAL_MILLIS", totalIntervalMillis) // Pass the loop gap forward
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Dispatch using the background-safe Doze-bypass slot (no security crash)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
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

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    /**
     * Completely tears down a scheduled system alarm event.
     * Bypasses ghost triggers if the user deletes a dashboard element.
     */
    fun cancel(reminderId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)

        // Reconstruct the exact structural signature flag identifier of the target alarm intent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Revoke the authorization from the Android system alarm registry
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}