package com.example.toodo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.toodo.worker.DailySummaryWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ToodoApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleDailySummaryWorker()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_TASK_REMINDER,
                    "任务提醒",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "DDL到期提醒通知"
                },
                NotificationChannel(
                    CHANNEL_DAILY_SUMMARY,
                    "每日概览",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "每日早晨待办摘要"
                },
                NotificationChannel(
                    CHANNEL_STREAK_REMINDER,
                    "打卡提醒",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "即将断打卡时提醒"
                }
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    private fun scheduleDailySummaryWorker() {
        val workRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayToNextMorning(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DAILY_SUMMARY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Calculates the delay in milliseconds until the next 8:00 AM.
     * If it is currently before 8 AM today, schedules for today at 8 AM.
     * Otherwise, schedules for tomorrow at 8 AM.
     */
    private fun calculateDelayToNextMorning(): Long {
        val now = LocalDateTime.now()
        val nextMorning = now.toLocalDate()
            .plusDays(if (now.hour >= 8) 1 else 0)
            .atTime(8, 0)
        return ChronoUnit.MILLIS.between(now, nextMorning)
    }

    companion object {
        const val CHANNEL_TASK_REMINDER = "task_reminder"
        const val CHANNEL_DAILY_SUMMARY = "daily_summary"
        const val CHANNEL_STREAK_REMINDER = "streak_reminder"

        private const val DAILY_SUMMARY_WORK_NAME = "daily_summary"
    }
}
