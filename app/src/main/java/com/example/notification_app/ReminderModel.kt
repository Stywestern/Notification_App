package com.example.notification_app

/**
 * Defines the two distinct categories of reminders in our system.
 */
enum class ReminderType {
    DAILY_PERSISTENT,  // For rolling/repeating tasks like "Drink water"
    EVENT_TIERED       // For one-off deadlines with warning stages
}

/**
 * The core blueprint representing a single reminder module.
 */
data class ReminderModel(
    val id: String,                 // Unique identifier to manage individual alarms
    val title: String,              // What the notification will actually say
    val type: ReminderType,         // Tells the orchestrator how to handle this item

    // Fields for DAILY_PERSISTENT tasks
    val intervalHours: Int? = null,  // e.g., Every 3 hours (null if specific moments)
    val specificMoments: List<String>? = null, // e.g., ["09:00", "14:00"]

    // Fields for EVENT_TIERED tasks
    val targetTimestamp: Long? = null, // The exact epoch millisecond the event arrives
    val warningIntervalHours: List<Int>? = null // e.g., [24, 1] for warnings 24h and 1h prior
)