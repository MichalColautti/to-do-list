package com.example.todolist

import android.content.Context
import androidx.core.content.edit

object SettingsManager {
    private const val PREFS_NAME = "settings"
    private const val NOTIFICATION_MINUTES_KEY = "notification_minutes"

    fun getNotificationMinutes(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(NOTIFICATION_MINUTES_KEY, 10)
    }

    fun setNotificationMinutes(context: Context, minutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit() { putInt(NOTIFICATION_MINUTES_KEY, minutes) }
    }
}
