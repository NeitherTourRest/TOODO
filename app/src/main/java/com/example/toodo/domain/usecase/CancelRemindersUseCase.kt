package com.example.toodo.domain.usecase

import com.example.toodo.domain.repository.TaskRepository
import com.example.toodo.notification.ReminderScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CancelRemindersUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(taskId: Long) {
        val reminders = taskRepository.getRemindersForTask(taskId).first()
        reminderScheduler.cancelAllRemindersForTask(taskId, reminders)
        taskRepository.deleteRemindersForTask(taskId)
    }
}
