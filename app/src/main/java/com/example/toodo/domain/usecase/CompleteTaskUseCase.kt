package com.example.toodo.domain.usecase

import com.example.toodo.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, isFocusTask: Boolean = false) {
        val todayEpochDay = LocalDate.now().toEpochDay()
        taskRepository.completeTask(taskId, todayEpochDay, isFocusTask)
    }
}
