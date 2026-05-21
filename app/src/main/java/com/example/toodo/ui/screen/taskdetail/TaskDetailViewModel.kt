package com.example.toodo.ui.screen.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.toodo.domain.model.SubTask
import com.example.toodo.domain.model.Task
import com.example.toodo.domain.repository.TaskRepository
import com.example.toodo.domain.usecase.DeleteTaskUseCase
import com.example.toodo.ui.navigation.TaskDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailUiState(
    val task: Task? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val taskId: Long = savedStateHandle.toRoute<TaskDetailRoute>().taskId

    init {
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            taskRepository.getTaskById(taskId).collectLatest { task ->
                _uiState.update { it.copy(task = task, isLoading = false) }
            }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }

    fun completeSubtask(subTask: SubTask) {
        if (subTask.isCompleted) return
        viewModelScope.launch {
            taskRepository.updateSubTask(subTask.copy(isCompleted = true))
        }
    }

    fun uncompleteSubtask(subTask: SubTask) {
        if (!subTask.isCompleted) return
        viewModelScope.launch {
            taskRepository.updateSubTask(subTask.copy(isCompleted = false))
        }
    }
}
