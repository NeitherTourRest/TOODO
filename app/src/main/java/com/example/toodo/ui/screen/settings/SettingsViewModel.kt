package com.example.toodo.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toodo.data.local.datastore.UserPreferences
import com.example.toodo.data.local.db.dao.*
import com.example.toodo.data.local.db.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** Combined UI state for the Settings screen. */
data class SettingsUiState(
    val darkMode: String = "system",
    val dailyResetHour: Int = 0,
    val dailyResetMinute: Int = 0,
    val defaultReminderOffsets: List<Long> = listOf(1440L, 60L),
    val notificationsEnabled: Boolean = true,
    val weekStartDay: Int = 1,
    val appVersion: String = "1.0"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val taskListDao: TaskListDao,
    private val taskDao: TaskDao,
    private val subTaskDao: SubTaskDao,
    private val recurrenceRuleDao: RecurrenceRuleDao,
    private val reminderConfigDao: ReminderConfigDao,
    private val completionRecordDao: CompletionRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Collect each preference flow and merge into uiState
        viewModelScope.launch {
            userPreferences.darkMode.collect { mode ->
                _uiState.update { it.copy(darkMode = mode) }
            }
        }
        viewModelScope.launch {
            userPreferences.dailyResetHour.collect { hour ->
                _uiState.update { it.copy(dailyResetHour = hour) }
            }
        }
        viewModelScope.launch {
            userPreferences.dailyResetMinute.collect { minute ->
                _uiState.update { it.copy(dailyResetMinute = minute) }
            }
        }
        viewModelScope.launch {
            userPreferences.defaultReminderOffsets.collect { offsets ->
                _uiState.update { it.copy(defaultReminderOffsets = offsets) }
            }
        }
        viewModelScope.launch {
            userPreferences.notificationsEnabled.collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferences.weekStartDay.collect { day ->
                _uiState.update { it.copy(weekStartDay = day) }
            }
        }
    }

    // ─── Preference Actions ──────────────────────────────────────────────────

    fun setDarkMode(mode: String) {
        viewModelScope.launch { userPreferences.setDarkMode(mode) }
    }

    fun setDailyResetTime(hour: Int, minute: Int) {
        viewModelScope.launch { userPreferences.setDailyResetTime(hour, minute) }
    }

    fun setDefaultReminderOffsets(offsets: List<Long>) {
        viewModelScope.launch { userPreferences.setDefaultReminderOffsets(offsets) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setNotificationsEnabled(enabled) }
    }

    fun setWeekStartDay(day: Int) {
        viewModelScope.launch { userPreferences.setWeekStartDay(day) }
    }

    // ─── JSON Export / Import ────────────────────────────────────────────────

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Collects all local data (lists, tasks, sub-tasks, recurrence rules,
     * reminders, completion records) and serializes them to a pretty-printed
     * JSON string suitable for writing to a file via SAF.
     */
    suspend fun buildExportJson(): String = withContext(Dispatchers.IO) {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val exportedAt = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        // ── Lists ────────────────────────────────────────────────────────────
        val listEntities = taskListDao.getAll().first()

        // ── Recurrence Rules ─────────────────────────────────────────────────
        val ruleEntities = recurrenceRuleDao.getAll().first()

        // ── Completion Records (wide range covers all history) ────────────────
        val completionEntities = completionRecordDao
            .getByDateRange(0, Long.MAX_VALUE).first()

        // ── Tasks ────────────────────────────────────────────────────────────
        val incompleteEntities = taskDao.searchTasks("").first()
        val completedTaskIds = completionEntities.map { it.taskId }.toSet()
        val completedTaskEntities = completedTaskIds
            .filter { tid -> incompleteEntities.none { it.id == tid } }
            .mapNotNull { taskDao.getById(it) }
        val allTaskEntities = incompleteEntities + completedTaskEntities

        // ── Sub-tasks ────────────────────────────────────────────────────────
        val subTaskEntities = allTaskEntities.flatMap { task ->
            subTaskDao.getByParentIdOnce(task.id)
        }

        // ── Reminders ────────────────────────────────────────────────────────
        val reminderEntities = allTaskEntities.flatMap { task ->
            reminderConfigDao.getByTaskIdOnce(task.id)
        }

        val exportData = ExportData(
            version = 1,
            exportedAt = exportedAt,
            lists = listEntities.map { it.toExportDto() },
            tasks = allTaskEntities.map { it.toExportDto() },
            subTasks = subTaskEntities.map { it.toExportDto() },
            recurrenceRules = ruleEntities.map { it.toExportDto() },
            reminderConfigs = reminderEntities.map { it.toExportDto() },
            completionRecords = completionEntities.map { it.toExportDto() }
        )

        json.encodeToString(exportData)
    }

    /**
     * Parses a JSON export string and inserts its contents into the database.
     * Handles ID conflicts by assigning new auto-generated IDs and maintaining
     * referential integrity via an old→new ID map.
     */
    suspend fun importFromJson(jsonString: String) = withContext(Dispatchers.IO) {
        val exportData = json.decodeFromString<ExportData>(jsonString)

        val listIdMap = mutableMapOf<Long, Long>()
        val taskIdMap = mutableMapOf<Long, Long>()
        val ruleIdMap = mutableMapOf<Long, Long>()

        // 1. Recurrence rules
        for (dto in exportData.recurrenceRules) {
            val entity = dto.toEntity()
            val newId = recurrenceRuleDao.insert(entity.copy(id = 0))
            ruleIdMap[dto.id] = newId
        }

        // 2. Lists
        for (dto in exportData.lists) {
            val entity = dto.toEntity()
            val newId = taskListDao.insert(entity.copy(id = 0))
            listIdMap[dto.id] = newId
        }

        // 3. Tasks (resolve listId and recurrenceRuleId)
        for (dto in exportData.tasks) {
            val entity = dto.toEntity(
                newListId = dto.listId?.let { listIdMap[it] },
                newRecurrenceRuleId = dto.recurrenceRuleId?.let { ruleIdMap[it] }
            )
            val newId = taskDao.insert(entity.copy(id = 0))
            taskIdMap[dto.id] = newId
        }

        // 4. Sub-tasks
        for (dto in exportData.subTasks) {
            val newParentId = taskIdMap[dto.parentId] ?: continue
            val entity = SubTaskEntity(
                id = 0, parentId = newParentId, title = dto.title,
                isCompleted = dto.isCompleted, sortOrder = dto.sortOrder
            )
            subTaskDao.insert(entity)
        }

        // 5. Reminders
        for (dto in exportData.reminderConfigs) {
            val newTaskId = taskIdMap[dto.taskId] ?: continue
            val entity = ReminderConfigEntity(
                id = 0, taskId = newTaskId,
                offsetMinutes = dto.offsetMinutes, isEnabled = dto.isEnabled
            )
            reminderConfigDao.insert(entity)
        }

        // 6. Completion records (REPLACE strategy avoids duplicate key violations)
        for (dto in exportData.completionRecords) {
            val newTaskId = taskIdMap[dto.taskId] ?: continue
            val entity = CompletionRecordEntity(
                id = 0, taskId = newTaskId, date = dto.date,
                completedAt = dto.completedAt, isFocusTask = dto.isFocusTask
            )
            completionRecordDao.insert(entity)
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  Export / Import DTOs
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class ExportData(
    val version: Int,
    val exportedAt: String,
    val lists: List<TaskListExport>,
    val tasks: List<TaskExport>,
    val subTasks: List<SubTaskExport>,
    val recurrenceRules: List<RecurrenceRuleExport>,
    val reminderConfigs: List<ReminderConfigExport>,
    val completionRecords: List<CompletionRecordExport>
)

@Serializable data class TaskListExport(val id: Long, val name: String, val iconName: String, val color: Long, val sortOrder: Int)

@Serializable
data class TaskExport(
    val id: Long, val title: String, val description: String, val taskType: String,
    val priority: Int, val listId: Long?, val dueDate: Long?, val dueTime: Int?,
    val recurrenceRuleId: Long?, val isFocusTask: Boolean, val isArchived: Boolean,
    val createdAt: Long, val completedAt: Long?, val sortOrder: Int
)

@Serializable data class SubTaskExport(val id: Long, val parentId: Long, val title: String, val isCompleted: Boolean, val sortOrder: Int)

@Serializable
data class RecurrenceRuleExport(
    val id: Long, val freq: String, val interval: Int, val byDay: String?,
    val byMonthDay: String?, val count: Int?, val untilDate: Long?
)

@Serializable data class ReminderConfigExport(val id: Long, val taskId: Long, val offsetMinutes: Long, val isEnabled: Boolean)

@Serializable data class CompletionRecordExport(val id: Long, val taskId: Long, val date: Long, val completedAt: Long, val isFocusTask: Boolean)

// ─── Entity → DTO mappers ────────────────────────────────────────────────────

private fun TaskListEntity.toExportDto() = TaskListExport(id, name, iconName, color, sortOrder)

private fun TaskEntity.toExportDto() = TaskExport(
    id, title, description, taskType, priority, listId, dueDate, dueTime,
    recurrenceRuleId, isFocusTask, isArchived, createdAt, completedAt, sortOrder
)

private fun SubTaskEntity.toExportDto() = SubTaskExport(id, parentId, title, isCompleted, sortOrder)

private fun RecurrenceRuleEntity.toExportDto() = RecurrenceRuleExport(
    id, freq, interval, byDay, byMonthDay, count, untilDate
)

private fun ReminderConfigEntity.toExportDto() = ReminderConfigExport(id, taskId, offsetMinutes, isEnabled)

private fun CompletionRecordEntity.toExportDto() = CompletionRecordExport(id, taskId, date, completedAt, isFocusTask)

// ─── DTO → Entity mappers (for import) ───────────────────────────────────────

private fun TaskListExport.toEntity() = TaskListEntity(
    id.coerceAtLeast(0), name, iconName, color, sortOrder
)

private fun TaskExport.toEntity(newListId: Long? = null, newRecurrenceRuleId: Long? = null) = TaskEntity(
    id.coerceAtLeast(0), title, description, taskType, priority,
    newListId ?: listId, dueDate, dueTime,
    newRecurrenceRuleId ?: recurrenceRuleId,
    isFocusTask, isArchived, createdAt, completedAt, sortOrder
)

private fun RecurrenceRuleExport.toEntity() = RecurrenceRuleEntity(
    id.coerceAtLeast(0), freq, interval, byDay, byMonthDay, count, untilDate
)
