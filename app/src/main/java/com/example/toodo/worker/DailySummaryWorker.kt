package com.example.toodo.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.toodo.ToodoApplication
import com.example.toodo.data.local.datastore.UserPreferences
import com.example.toodo.domain.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val userPreferences: UserPreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val notificationsEnabled = userPreferences.notificationsEnabled.first()
            if (!notificationsEnabled) return Result.success()

            val todayEpochDay = LocalDate.now().toEpochDay()
            val tasks = taskRepository.getTodayTasks(todayEpochDay).first()

            val totalTasks = tasks.size
            val completedTasks = tasks.count { it.isCompleted }

            if (totalTasks == 0) return Result.success()

            val summaryText = "今日待办: $completedTasks/$totalTasks 已完成"

            val notification = NotificationCompat.Builder(applicationContext, ToodoApplication.CHANNEL_DAILY_SUMMARY)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("每日待办概览")
                .setContentText(summaryText)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            NotificationManagerCompat.from(applicationContext)
                .notify(NOTIFICATION_ID_DAILY_SUMMARY, notification)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val NOTIFICATION_ID_DAILY_SUMMARY = 1001
    }
}
