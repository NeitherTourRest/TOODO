package com.example.toodo.ui.screen.addtask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.toodo.domain.model.*
import com.example.toodo.domain.repository.TaskListRepository
import com.example.toodo.domain.repository.TaskRepository
import com.example.toodo.domain.usecase.CreateTaskUseCase
import com.example.toodo.domain.usecase.UpdateTaskUseCase
import com.example.toodo.ui.navigation.AddTaskRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

enum class UiTaskType(val label: String) {
    ONE_TIME("一次性"),
    DAILY("每日"),
    RECURRING("循环"),
    DDL("DDL")
}

data class AddTaskUiState(
    val title: String = "",
    val description: String = "",
    val taskType: TaskType = TaskType.ONE_TIME,
    val uiTaskType: UiTaskType = UiTaskType.ONE_TIME,
    val dueDate: LocalDate? = LocalDate.now(),
    val dueTime: LocalTime? = null,
    val priority: Priority = Priority.MEDIUM,
    val selectedListId: Long? = null,
    val isFocusTask: Boolean = false,
    val recurrenceFreq: Frequency = Frequency.WEEKLY,
    val recurrenceInterval: Int = 1,
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val selectedMonthDays: Set<Int> = emptySet(),
    val reminderOffsets: List<Long> = emptyList(),
    val subTasks: List<SubTaskDraft> = emptyList(),
    val availableLists: List<TaskList> = emptyList(),
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false
)

data class SubTaskDraft(val tempId: Int, val title: String, val isCompleted: Boolean = false)

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val taskRepository: TaskRepository,
    private val taskListRepository: TaskListRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    private var nextSubTaskTempId = 0
    private val taskId: Long? = savedStateHandle.toRoute<AddTaskRoute>().taskId

    init {
        viewModelScope.launch {
            taskListRepository.getAllLists().collect { lists ->
                _uiState.update { it.copy(availableLists = lists) }
            }
        }

        taskId?.let { id ->
            loadExistingTask(id)
        }
    }

    private fun loadExistingTask(id: Long) {
        viewModelScope.launch {
            taskRepository.getTaskById(id).first()?.let { task ->
                _uiState.update {
                    it.copy(
                        title = task.title,
                        description = task.description,
                        taskType = task.taskType,
                        uiTaskType = mapDomainToUiType(task),
                        dueDate = task.dueDate,
                        dueTime = task.dueTime,
                        priority = task.priority,
                        selectedListId = task.listId,
                        isFocusTask = task.isFocusTask,
                        recurrenceFreq = task.recurrenceRule?.freq ?: Frequency.WEEKLY,
                        recurrenceInterval = task.recurrenceRule?.interval ?: 1,
                        selectedDays = task.recurrenceRule?.byDay ?: emptySet(),
                        selectedMonthDays = task.recurrenceRule?.byMonthDay ?: emptySet(),
                        reminderOffsets = task.reminders.map { it.offsetMinutes },
                        subTasks = task.subTasks.mapIndexed { index, st ->
                            SubTaskDraft(tempId = index, title = st.title, isCompleted = st.isCompleted)
                        },
                        isEditMode = true
                    )
                }
                nextSubTaskTempId = task.subTasks.size
            }
        }
    }

    private fun mapDomainToUiType(task: Task): UiTaskType {
        if (task.taskType == TaskType.ONE_TIME && task.reminders.isNotEmpty()) {
            return UiTaskType.DDL
        }
        return when (task.taskType) {
            TaskType.ONE_TIME -> UiTaskType.ONE_TIME
            TaskType.DAILY -> UiTaskType.DAILY
            TaskType.RECURRING -> UiTaskType.RECURRING
        }
    }

    // ── Title ──
    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, titleError = false) }
    }

    // ── Description / Notes ──
    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    // ── Task type tabs ──
    fun onUiTaskTypeChange(type: UiTaskType) {
        _uiState.update { state ->
            when (type) {
                UiTaskType.ONE_TIME -> state.copy(
                    uiTaskType = type,
                    taskType = TaskType.ONE_TIME,
                    dueDate = state.dueDate ?: LocalDate.now(),
                    reminderOffsets = emptyList(),
                    selectedDays = emptySet(),
                    selectedMonthDays = emptySet()
                )
                UiTaskType.DAILY -> state.copy(
                    uiTaskType = type,
                    taskType = TaskType.DAILY,
                    dueDate = null,
                    dueTime = null,
                    reminderOffsets = emptyList(),
                    selectedDays = emptySet(),
                    selectedMonthDays = emptySet()
                )
                UiTaskType.RECURRING -> state.copy(
                    uiTaskType = type,
                    taskType = TaskType.RECURRING,
                    dueDate = null,
                    dueTime = null,
                    reminderOffsets = emptyList()
                )
                UiTaskType.DDL -> state.copy(
                    uiTaskType = type,
                    taskType = TaskType.ONE_TIME,
                    dueDate = state.dueDate ?: LocalDate.now()
                )
            }
        }
    }

    // ── Date / Time ──
    fun onDueDateChange(date: LocalDate?) {
        _uiState.update { it.copy(dueDate = date) }
    }

    fun onDueTimeChange(time: LocalTime?) {
        _uiState.update { it.copy(dueTime = time) }
    }

    fun onShowDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onHideDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun onShowTimePicker() {
        _uiState.update { it.copy(showTimePicker = true) }
    }

    fun onHideTimePicker() {
        _uiState.update { it.copy(showTimePicker = false) }
    }

    // ── Priority ──
    fun onPriorityChange(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    // ── Category / List ──
    fun onListChange(listId: Long?) {
        _uiState.update { it.copy(selectedListId = listId) }
    }

    // ── Focus toggle ──
    fun onFocusToggle() {
        _uiState.update { it.copy(isFocusTask = !it.isFocusTask) }
    }

    // ── Recurrence ──
    fun onRecurrenceFreqChange(freq: Frequency) {
        _uiState.update { it.copy(recurrenceFreq = freq, selectedDays = emptySet(), selectedMonthDays = emptySet()) }
    }

    fun onRecurrenceIntervalChange(interval: Int) {
        if (interval < 1) return
        _uiState.update { it.copy(recurrenceInterval = interval) }
    }

    fun onDayToggle(day: DayOfWeek) {
        _uiState.update { state ->
            val newDays = state.selectedDays.toMutableSet().apply {
                if (contains(day)) remove(day) else add(day)
            }
            state.copy(selectedDays = newDays)
        }
    }

    fun onMonthDayToggle(day: Int) {
        _uiState.update { state ->
            val newDays = state.selectedMonthDays.toMutableSet().apply {
                if (contains(day)) remove(day) else add(day)
            }
            state.copy(selectedMonthDays = newDays)
        }
    }

    // ── Reminders ──
    fun onReminderOffsetToggle(offsetMinutes: Long) {
        _uiState.update { state ->
            val offsets = state.reminderOffsets.toMutableList()
            if (offsets.contains(offsetMinutes)) {
                offsets.remove(offsetMinutes)
            } else {
                offsets.add(offsetMinutes)
            }
            state.copy(reminderOffsets = offsets.sortedByDescending { it })
        }
    }

    fun onAddCustomReminder(offsetMinutes: Long) {
        if (offsetMinutes <= 0) return
        _uiState.update { state ->
            val offsets = state.reminderOffsets.toMutableList().apply {
                if (!contains(offsetMinutes)) add(offsetMinutes)
            }
            state.copy(reminderOffsets = offsets.sortedByDescending { it })
        }
    }

    fun onRemoveReminder(offsetMinutes: Long) {
        _uiState.update { state ->
            state.copy(reminderOffsets = state.reminderOffsets - offsetMinutes)
        }
    }

    // ── Subtasks ──
    fun onAddSubTask() {
        val draft = SubTaskDraft(tempId = nextSubTaskTempId++, title = "")
        _uiState.update { it.copy(subTasks = it.subTasks + draft) }
    }

    fun onUpdateSubTask(tempId: Int, title: String) {
        _uiState.update { state ->
            state.copy(
                subTasks = state.subTasks.map {
                    if (it.tempId == tempId) it.copy(title = title) else it
                }
            )
        }
    }

    fun onToggleSubTask(tempId: Int) {
        _uiState.update { state ->
            state.copy(
                subTasks = state.subTasks.map {
                    if (it.tempId == tempId) it.copy(isCompleted = !it.isCompleted) else it
                }
            )
        }
    }

    fun onDeleteSubTask(tempId: Int) {
        _uiState.update { state ->
            val filtered = state.subTasks.filter { it.tempId != tempId }
            state.copy(subTasks = filtered)
        }
    }

    // ── Save ──
    fun onSave(onNavigateBack: () -> Unit) {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val state = _uiState.value
            val domainTaskType = state.taskType
            val hasDueDateTime = state.uiTaskType == UiTaskType.ONE_TIME || state.uiTaskType == UiTaskType.DDL

            val recurrenceRule: RecurrenceRule? = when (state.uiTaskType) {
                UiTaskType.RECURRING -> RecurrenceRule(
                    freq = state.recurrenceFreq,
                    interval = state.recurrenceInterval,
                    byDay = state.selectedDays,
                    byMonthDay = state.selectedMonthDays
                )
                UiTaskType.DAILY -> RecurrenceRule(
                    freq = Frequency.DAILY,
                    interval = 1
                )
                else -> null
            }

            val reminders: List<ReminderConfig> = if (state.uiTaskType == UiTaskType.DDL) {
                state.reminderOffsets.map { offset ->
                    ReminderConfig(offsetMinutes = offset)
                }
            } else emptyList()

            val subTasks: List<SubTask> = state.subTasks
                .filter { it.title.isNotBlank() }
                .map { draft ->
                    SubTask(parentId = 0, title = draft.title.trim(), isCompleted = draft.isCompleted)
                }

            val task = Task(
                id = taskId ?: 0,
                title = state.title.trim(),
                description = state.description.trim(),
                taskType = domainTaskType,
                priority = state.priority,
                listId = state.selectedListId,
                dueDate = if (hasDueDateTime) state.dueDate else null,
                dueTime = if (hasDueDateTime) state.dueTime else null,
                isFocusTask = state.isFocusTask
            )

            if (state.isEditMode && taskId != null) {
                updateTaskUseCase(task.copy(id = taskId))
            } else {
                createTaskUseCase(task, recurrenceRule, reminders, subTasks)
            }

            onNavigateBack()
        }
    }
}
