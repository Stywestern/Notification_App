package com.example.notification_app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import java.util.Calendar
import java.util.Locale

/**
 * A utility module handling custom sliding picker dialog layouts.
 */
object PickerUtils {

    /**
     * Spawns a custom native pop-up alert containing three horizontal sliding selection wheels.
     */
    fun showSlidingDurationPicker(
        context: Context,
        title: String,
        onDurationSelected: (hours: Int, minutes: Int, seconds: Int) -> Unit
    ) {
        // 1. Create a container layout to hold the wheels horizontally
        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 32, 16, 32)
        }

        // Helper parameters to initialize a clean wheel design instance
        fun createWheel(max: Int, labelText: String): NumberPicker {
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val label = TextView(context).apply {
                text = labelText
                gravity = Gravity.CENTER
            }

            val picker = NumberPicker(context).apply {
                minValue = 0
                maxValue = max
            }

            container.addView(label)
            container.addView(picker)
            linearLayout.addView(container)
            return picker
        }

        // 2. Instantiate our sliding duration barrel components
        val hourPicker = createWheel(23, "Hours")
        val minutePicker = createWheel(59, "Minutes")
        val secondPicker = createWheel(59, "Seconds")

        // 3. Mount the layout elements onto a standard popup alert frame
        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(linearLayout)
            .setPositiveButton("Confirm") { dialog, _ ->
                onDurationSelected(hourPicker.value, minutePicker.value, secondPicker.value)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Shows a native date picker followed immediately by our modern sliding barrel duration picker.
     */
    fun showDateTimePicker(context: Context, onDateTimeSelected: (Long, String) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Date is set! Now chain-launch our custom sliding duration barrel dialog
                showSlidingDurationPicker(context, "Select Exact Event Time") { hour, minute, second ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, second)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val timestamp = calendar.timeInMillis
                    val readableText = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%d %02d:%02d:%02d",
                        dayOfMonth, month + 1, year, hour, minute, second
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