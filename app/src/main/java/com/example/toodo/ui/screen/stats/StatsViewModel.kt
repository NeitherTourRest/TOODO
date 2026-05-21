package com.example.toodo.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toodo.domain.repository.StatsRepository
import com.example.toodo.domain.repository.TaskListRepository
import com.example.toodo.domain.repository.TaskRepository
import com.example.toodo.domain.usecase.GetDailyStatsUseCase
import com.example.toodo.domain.usecase.GetStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StatsUiState(
    val streakDays: Int = 0,
    val longestStreak: Int = 0,
    val freezesRemaining: Int = 0,
    val todayCompletedCount: Int = 0,
    val todayTotalCount: Int = 0,
    val todayFocusCompletedCount: Int = 0,
    val heatmapData: Map<Long, Int> = emptyMap(),
    val weeklyData: Map<Int, Int> = emptyMap(),
    val categoryData: Map<String, Int> = emptyMap(),
    val categoryColors: Map<String, Long> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getDailyStatsUseCase: GetDailyStatsUseCase,
    private val getStreakUseCase: GetStreakUseCase,
    private val statsRepository: StatsRepository,
    private val taskRepository: TaskRepository,
    private val taskListRepository: TaskListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val today = LocalDate.now()
            val todayEpochDay = today.toEpochDay()

            // 1. Streak info (suspend)
            val streakInfo = getStreakUseCase()

            // 2. Today's stats (suspend)
            val todayStats = getDailyStatsUseCase()
            // Count today's total tasks from repository
            val todayTasks = taskRepository.getTodayTasks(todayEpochDay).first()
            val todayTotal = todayTasks.size

            // 3. Completion records for past 365 days
            val records365 = statsRepository
                .getCompletionRecordsBetween(todayEpochDay - 365, todayEpochDay)
                .first()

            // 4. Heatmap: group by epochDay → count
            val heatmapData = records365
                .groupBy { it.date }
                .mapValues { (_, recs) -> recs.size }

            // 5. Weekly: past 28 days, group by dayOfWeek (1=Mon..7=Sun)
            val weeklyData = records365
                .filter { it.date >= todayEpochDay - 28 }
                .groupBy { record ->
                    LocalDate.ofEpochDay(record.date).dayOfWeek.value
                }
                .mapValues { (_, recs) -> recs.size }

            // 6. Category: past 30 days, map through task → list
            val lists = taskListRepository.getAllLists().first()
            val listIdToName = lists.associate { it.id to it.name }
            val listIdToColor = lists.associate { it.id to it.color }

            val recentRecords = records365.filter { it.date >= todayEpochDay - 30 }

            // Build taskId → listId cache to minimise DB lookups
            val uniqueTaskIds = recentRecords.map { it.taskId }.distinct()
            val taskIdToListId = mutableMapOf<Long, Long>()
            for (taskId in uniqueTaskIds) {
                val task = taskRepository.getTaskById(taskId).first()
                if (task != null) {
                    taskIdToListId[taskId] = task.listId ?: 0L
                }
            }

            val categoryMap = mutableMapOf<String, Int>()
            val categoryColors = mutableMapOf<String, Long>()
            for (record in recentRecords) {
                val listId = taskIdToListId[record.taskId] ?: continue
                val listName = listIdToName[listId] ?: "未分类"
                categoryMap[listName] = (categoryMap[listName] ?: 0) + 1
                categoryColors.putIfAbsent(listName, listIdToColor[listId] ?: 0xFF6750A4)
            }

            _uiState.update {
                it.copy(
                    streakDays = streakInfo.currentStreak,
                    longestStreak = streakInfo.longestStreak,
                    freezesRemaining = streakInfo.freezesRemaining,
                    todayCompletedCount = todayStats.completedCount,
                    todayTotalCount = todayTotal,
                    todayFocusCompletedCount = todayStats.focusCompletedCount,
                    heatmapData = heatmapData,
                    weeklyData = weeklyData,
                    categoryData = categoryMap,
                    categoryColors = categoryColors,
                    isLoading = false
                )
            }
        }
    }

    fun useFreeze() {
        viewModelScope.launch {
            val todayEpochDay = LocalDate.now().toEpochDay()
            statsRepository.useFreeze(todayEpochDay)
            loadStats()
        }
    }
}
