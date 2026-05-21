package com.example.toodo.ui.screen.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toodo.domain.model.Task
import com.example.toodo.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class PlanUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val weekDates: List<LocalDate> = emptyList(),
    val tasksForSelectedDate: List<Task> = emptyList(),
    val monthTaskCounts: Map<LocalDate, Int> = emptyMap(),
    val searchQuery: String = "",
    val searchResults: List<Task> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _searchQuery = MutableStateFlow("")
    private val _weekDates = MutableStateFlow(generateWeekDates())

    @OptIn(FlowPreview::class)
    private val tasksForDate: Flow<List<Task>> = _selectedDate
        .flatMapLatest { date ->
            val startMillis = date.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            val endMillis = date.plusDays(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - 1
            taskRepository.getTasksByDateRange(startMillis, endMillis)
        }

    @OptIn(FlowPreview::class)
    private val searchResultsFlow: Flow<List<Task>> = _searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else taskRepository.searchTasks(query)
        }

    @OptIn(FlowPreview::class)
    private val monthTaskCounts: Flow<Map<LocalDate, Int>> = _currentMonth
        .flatMapLatest { month ->
            val startMillis = month.atDay(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            val endMillis = month.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - 1
            taskRepository.getTasksByDateRange(startMillis, endMillis).map { tasks ->
                tasks.groupBy { it.dueDate!! }.mapValues { it.value.size }
            }
        }

    val uiState: StateFlow<PlanUiState> = combine(
        _selectedDate,
        _currentMonth,
        _searchQuery,
        _weekDates,
        tasksForDate.onStart { emit(emptyList()) },
        monthTaskCounts.onStart { emit(emptyMap()) },
        searchResultsFlow.onStart { emit(emptyList()) }
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val date = args[0] as LocalDate
        val month = args[1] as YearMonth
        val query = args[2] as String
        val weekDates = args[3] as List<LocalDate>
        val tasks = args[4] as List<Task>
        val counts = args[5] as Map<LocalDate, Int>
        val searchResults = args[6] as List<Task>
        PlanUiState(
            selectedDate = date,
            currentMonth = month,
            weekDates = weekDates,
            tasksForSelectedDate = tasks,
            monthTaskCounts = counts,
            searchQuery = query,
            searchResults = searchResults,
            isSearching = query.isNotBlank(),
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanUiState())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }

    fun previousMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }

    fun jumpToToday() {
        _selectedDate.value = LocalDate.now()
        _weekDates.value = generateWeekDates()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    private fun generateWeekDates(): List<LocalDate> {
        val today = LocalDate.now()
        return (0..6).map { today.plusDays(it.toLong()) }
    }
}
