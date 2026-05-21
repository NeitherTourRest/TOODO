package com.example.toodo.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.toodo.domain.model.ReminderConfig
import com.example.toodo.domain.repository.TaskRepository
import com.example.toodo.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository
) {
    companion object {
        const val ACTION_REMINDER = "com.example.toodo.ACTION_REMINDER"
        const val EXTRA_TASK_ID = "task_id"
    }

    private val alarmManager: AlarmManager
        get() = context.getSystemService(AlarmManager::class.java)

    /**
     * Schedules an alarm-based reminder for a task at the calculated trigger time.
     * Uses setExactAndAllowWhileIdle on API 31+, falls back to setAndAllowWhileIdle.
     * Skips scheduling if dueDateMillis is null or if the trigger time is already past.
     */
    fun scheduleReminder(
        taskId: Long,
        dueDateMillis: Long?,
        dueTimeMinutes: Int?,
        offsetMinutes: Long
    ) {
        if (dueDateMillis == null) return

        val zoneId = ZoneId.systemDefault()
        val dueDate = Instant.ofEpochMilli(dueDateMillis).atZone(zoneId).toLocalDate()
        val dueTime = if (dueTimeMinutes != null) {
            LocalTime.of(dueTimeMinutes / 60, dueTimeMinutes % 60)
        } else {
            LocalTime.MIDNIGHT
        }

        val dueDateTime = dueDate.atTime(dueTime).atZone(zoneId).toInstant()
        val triggerTime = dueDateTime.toEpochMilli() - offsetMinutes * 60 * 1000

        // Don't schedule alarms in the past
        val now = System.currentTimeMillis()
        if (triggerTime <= now) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_TASK_ID, taskId)
        }

        val requestCode = (taskId * 1000 + offsetMinutes).toInt()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
            )
        }
    }

    /**
     * Cancels a single reminder alarm identified by taskId and offsetMinutes.
     */
    fun cancelReminder(taskId: Long, offsetMinutes: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_REMINDER
        }
        val requestCode = (taskId * 1000 + offsetMinutes).toInt()
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        pendingIntent?.cancel()
    }

    /**
     * Cancels all scheduled alarms for a task across all its reminder configurations.
     */
    fun cancelAllRemindersForTask(taskId: Long, reminders: List<ReminderConfig>) {
        for (reminder in reminders) {
            cancelReminder(taskId, reminder.offsetMinutes)
        }
    }

    /**
     * Queries all tasks with future due dates from the repository and reschedules
     * their reminder alarms. Called on boot to restore alarms after device restart.
     */
    suspend fun rescheduleAllReminders() {
        val zoneId = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val farFuture = now + 365L * 24 * 60 * 60 * 1000 // 365 days ahead

        val tasks = taskRepository.getTasksByDateRange(now, farFuture).first()

        for (task in tasks) {
            if (task.reminders.isEmpty()) continue
            val dueDateEpochDay = task.dueDate?.toEpochDay() ?: continue
            val dueDateMillis = LocalDate.ofEpochDay(dueDateEpochDay)
                .atStartOfDay(zoneId).toInstant().toEpochMilli()
            val dueTimeMinutes = task.dueTime?.let { it.hour * 60 + it.minute }

            for (reminder in task.reminders) {
                if (!reminder.isEnabled) continue
                scheduleReminder(task.id, dueDateMillis, dueTimeMinutes, reminder.offsetMinutes)
            }
        }
    }
}
