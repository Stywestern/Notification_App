package com.example.notification_app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * An isolated module responsible for reading and writing reminders to disk.
 */
class ReminderStorage(context: Context) {

    // Android's built-in key-value private storage engine
    private val sharedPreferences = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Saves the entire list of reminders to local storage.
     */
    fun saveReminders(reminders: List<ReminderModel>) {
        val jsonString = gson.toJson(reminders)
        sharedPreferences.edit().putString("all_reminders", jsonString).apply()
    }

    /**
     * Fetches all saved reminders from local storage. Returns an empty list if none exist.
     */
    fun getReminders(): List<ReminderModel> {
        val jsonString = sharedPreferences.getString("all_reminders", null) ?: return emptyList()

        // Because of Kotlin's type safety, we have to explicitly tell Gson
        // that we expect it to reconstruct a List of ReminderModels.
        val type = object : TypeToken<List<ReminderModel>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}

// Dummy line for annoying IDE
class Idle(context: Context){}