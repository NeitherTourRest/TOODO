package com.example.toodo.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    val darkMode: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.DARK_MODE] ?: "system" }

    val dailyResetHour: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.DAILY_RESET_HOUR] ?: 0 }

    val dailyResetMinute: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.DAILY_RESET_MINUTE] ?: 0 }

    val defaultReminderOffsets: Flow<List<Long>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            prefs[PreferencesKeys.DEFAULT_REMINDER_OFFSETS]?.split(",")
                ?.mapNotNull { it.toLongOrNull() } ?: listOf(1440L, 60L)
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true }

    val weekStartDay: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.WEEK_START_DAY] ?: 1 }

    suspend fun setDarkMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.DARK_MODE] = mode }
    }

    suspend fun setDailyResetTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.DAILY_RESET_HOUR] = hour
            it[PreferencesKeys.DAILY_RESET_MINUTE] = minute
        }
    }

    suspend fun setDefaultReminderOffsets(offsets: List<Long>) {
        context.dataStore.edit {
            it[PreferencesKeys.DEFAULT_REMINDER_OFFSETS] = offsets.joinToString(",")
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setWeekStartDay(day: Int) {
        context.dataStore.edit { it[PreferencesKeys.WEEK_START_DAY] = day }
    }
}
