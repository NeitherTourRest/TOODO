package com.example.toodo.ui.screen.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toodo.domain.model.Task
import com.example.toodo.domain.usecase.CompleteTaskUseCase
import com.example.toodo.domain.usecase.DeleteTaskUseCase
import com.example.toodo.domain.usecase.GetDailyStatsUseCase
import com.example.toodo.domain.usecase.GetStreakUseCase
import com.example.toodo.domain.usecase.GetTodayTasksUseCase
import com.example.toodo.domain.usecase.UncompleteTaskUseCase
import com.example.toodo.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayTasksUseCase: GetTodayTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val uncompleteTaskUseCase: UncompleteTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val getStreakUseCase: GetStreakUseCase,
    private val getDailyStatsUseCase: GetDailyStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    /** Task from the 4th+ focus attempt that was rejected — UI should show a snackbar. */
    private val _showFocusLimitReached = MutableStateFlow(false)
    val showFocusLimitReached: StateFlow<Boolean> = _showFocusLimitReached.asStateFlow()

    private var allTasks: List<Task> = emptyList()

    init {
        // Collect today's tasks from Room Flow
        viewModelScope.launch {
            getTodayTasksUseCase().collect { tasks ->
                allTasks = tasks
                refreshStreak()
                buildUiState(tasks)
            }
        }
    }

    private suspend fun refreshStreak() {
        val streak = getStreakUseCase()
        _uiState.update { it.copy(streakDays = streak.currentStreak, freezesRemaining = streak.freezesRemaining) }
    }

    private fun buildUiState(tasks: List<Task>) {
        val sorted = tasks.sortedWith(
            compareBy<Task> { it.isCompleted }
                .thenByDescending { it.priority.value }
                .thenBy { it.sortOrder }
        )
        val focused = sorted.filter { it.isFocusTask && !it.isCompleted }
        val today = sorted.filter { !it.isFocusTask && !it.isCompleted }
        val completed = sorted.filter { it.isCompleted }

        _uiState.update {
            it.copy(
                focusedTasks = focused,
                todayTasks = today,
                completedTasks = completed,
                todayCompletedCount = completed.size,
                todayTotalCount = tasks.size,
                isLoading = false
            )
        }
    }

    // ──────────────────────────────────────────────
    //  Intent functions
    // ──────────────────────────────────────────────

    fun completeTask(task: Task) {
        viewModelScope.launch {
            completeTaskUseCase(task.id, isFocusTask = task.isFocusTask)
            if (task.isFocusTask) {
                triggerConfetti()
            }
        }
    }

    fun uncompleteTask(task: Task) {
        viewModelScope.launch {
            uncompleteTaskUseCase(task.id)
        }
    }

    fun deferTask(task: Task) {
        viewModelScope.launch {
            val tomorrow = LocalDate.now().plusDays(1)
            updateTaskUseCase(task.copy(dueDate = tomorrow))
        }
    }

    fun toggleFocus(task: Task) {
        viewModelScope.launch {
            val currentlyFocused = allTasks.filter { it.isFocusTask && it.id != task.id }.size
            if (!task.isFocusTask && currentlyFocused >= 3) {
                // Reject — already 3 other focus tasks
                _showFocusLimitReached.value = true
                return@launch
            }
            updateTaskUseCase(task.copy(isFocusTask = !task.isFocusTask))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            deleteTaskUseCase(task.id)
            dismissBottomSheet()
        }
    }

    fun selectTask(task: Task) {
        _uiState.update { it.copy(selectedTask = task, showBottomSheet = true) }
    }

    fun dismissBottomSheet() {
        _uiState.update { it.copy(selectedTask = null, showBottomSheet = false) }
    }

    fun dismissFocusLimitSnackbar() {
        _showFocusLimitReached.value = false
    }

    // ──────────────────────────────────────────────
    //  Confetti
    // ──────────────────────────────────────────────

    private fun triggerConfetti() {
        viewModelScope.launch {
            _uiState.update { it.copy(showConfetti = true) }
            delay(800)
            _uiState.update { it.copy(showConfetti = false) }
        }
    }
}
