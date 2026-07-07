package com.example.notification_app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.util.Calendar
import java.util.Locale

/**
 * A utility module handling native OS picker overlays.
 */
object PickerUtils {

    /**
     * Shows a native system time picker and formats the result as HH:mm:ss.
     */
    fun showTimePicker(context: Context, onTimeSelected: (String, Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                // Format directly into type-safe HH:mm:00 string layout
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute)
                onTimeSelected(formattedTime, hourOfDay, minute)
            },
            currentHour,
            currentMinute,
            true // True forces 24-hour mode layout
        ).show()
    }

    /**
     * Shows a native date picker followed immediately by a time picker to capture a full timestamp.
     */
    fun showDateTimePicker(context: Context, onDateTimeSelected: (Long, String) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // Store the date parts into our calendar engine
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Date selected! Now launch the time picker sequentially
                showTimePicker(context) { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val timestamp = calendar.timeInMillis
                    val readableText = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%d %02d:%02d:00",
                        dayOfMonth, month + 1, year, hour, minute
                    )

                    onDateTimeSelected(timestamp, readableText)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}