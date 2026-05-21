package com.example.toodo.domain.usecase

import com.example.toodo.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class UncompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long) {
        val todayEpochDay = LocalDate.now().toEpochDay()
        taskRepository.uncompleteTask(taskId, todayEpochDay)
    }
}
