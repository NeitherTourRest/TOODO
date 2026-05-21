package com.example.toodo.data.local.datastore

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey

object PreferencesKeys {
    val DARK_MODE = stringPreferencesKey("dark_mode")
    val DAILY_RESET_HOUR = intPreferencesKey("daily_reset_hour")
    val DAILY_RESET_MINUTE = intPreferencesKey("daily_reset_minute")
    val DEFAULT_REMINDER_OFFSETS = stringPreferencesKey("default_reminder_offsets")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val WEEK_START_DAY = intPreferencesKey("week_start_day")
}
