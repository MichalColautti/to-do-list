package com.example.todolist

import android.content.Context
import androidx.core.content.edit

object SettingsManager {
    private const val PREFS_NAME = "settings"
    private const val NOTIFICATION_MINUTES_KEY = "notification_minutes"

    private const val HIDE_COMPLETED_KEY = "hide_completed"
    private const val SORT_OPTION_KEY = "sort_option"
    private const val CATEGORY_KEY = "selected_category"

    fun getNotificationMinutes(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(NOTIFICATION_MINUTES_KEY, 10)
    }

    fun setNotificationMinutes(context: Context, minutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(NOTIFICATION_MINUTES_KEY, minutes) }
    }

    fun setHideCompleted(context: Context, hide: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(HIDE_COMPLETED_KEY, hide) }
    }

    fun getHideCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(HIDE_COMPLETED_KEY, false)
    }

    fun setSortOption(context: Context, sort: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(SORT_OPTION_KEY, sort) }
    }

    fun getSortOption(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SORT_OPTION_KEY, SortOption.DATE_ASC.name) ?: SortOption.DATE_ASC.name
    }

    fun setCategory(context: Context, category: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(CATEGORY_KEY, category) }
    }

    fun getCategory(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(CATEGORY_KEY, "Wszystkie") ?: "Wszystkie"
    }
}
