package com.example.toodo.domain.usecase

import com.example.toodo.domain.model.*
import com.example.toodo.domain.repository.TaskRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        task: Task,
        recurrenceRule: RecurrenceRule? = null,
        reminders: List<ReminderConfig> = emptyList(),
        subTasks: List<SubTask> = emptyList()
    ): Long {
        return taskRepository.createTask(task, recurrenceRule, reminders, subTasks)
    }
}
