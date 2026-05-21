package com.example.toodo.ui.screen.today

import com.example.toodo.domain.model.Task

data class TodayUiState(
    val focusedTasks: List<Task> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val streakDays: Int = 0,
    val freezesRemaining: Int = 0,
    val todayCompletedCount: Int = 0,
    val todayTotalCount: Int = 0,
    val isLoading: Boolean = true,
    val selectedTask: Task? = null,
    val showBottomSheet: Boolean = false,
    val showConfetti: Boolean = false
)
