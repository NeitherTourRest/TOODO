package com.example.toodo.domain.usecase

import com.example.toodo.domain.model.Task
import com.example.toodo.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTodayTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        val todayEpochDay = LocalDate.now().toEpochDay()
        return taskRepository.getTodayTasks(todayEpochDay)
    }
}
