package com.example.toodo.ui.screen.tasklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toodo.domain.model.Task
import com.example.toodo.domain.model.TaskList
import com.example.toodo.domain.repository.TaskListRepository
import com.example.toodo.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskListRepository: TaskListRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val listId: Long = savedStateHandle.get<Long>("listId") ?: 0L

    val tasks: StateFlow<List<Task>> = taskRepository
        .getTasksByList(listId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val taskCount: StateFlow<Int> = tasks
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _taskList = MutableStateFlow<TaskList?>(null)
    val taskList: StateFlow<TaskList?> = _taskList.asStateFlow()

    init {
        loadTaskList()
    }

    private fun loadTaskList() {
        viewModelScope.launch {
            _taskList.value = taskListRepository.getListById(listId)
        }
    }
}
