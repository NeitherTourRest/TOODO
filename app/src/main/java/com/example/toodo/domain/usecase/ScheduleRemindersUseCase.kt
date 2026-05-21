package com.example.toodo.domain.usecase

import com.example.toodo.domain.model.ReminderConfig
import com.example.toodo.domain.repository.TaskRepository
import com.example.toodo.notification.ReminderScheduler
import javax.inject.Inject

class ScheduleRemindersUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(
        taskId: Long,
        dueDateMillis: Long?,
        dueTimeMinutes: Int?,
        reminderOffsets: List<Long>
    ) {
        for (offsetMinutes in reminderOffsets) {
            reminderScheduler.scheduleReminder(taskId, dueDateMillis, dueTimeMinutes, offsetMinutes)

            val config = ReminderConfig(
                taskId = taskId,
                offsetMinutes = offsetMinutes,
                isEnabled = true
            )
            taskRepository.addReminderConfig(config)
        }
    }
}
