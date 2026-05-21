package com.example.toodo.domain.usecase

import com.example.toodo.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Skips a daily/recurring task for today by writing a CompletionRecord
 * WITHOUT setting completedAt. This hides the task from today's view
 * without counting it toward streaks or stats.
 */
class SkipTodayTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long) {
        val todayEpochDay = LocalDate.now().toEpochDay()
        taskRepository.skipTodayTask(taskId, todayEpochDay)
    }
}
