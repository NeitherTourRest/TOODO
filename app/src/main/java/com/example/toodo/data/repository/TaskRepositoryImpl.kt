package com.example.toodo.data.repository

import com.example.toodo.data.local.db.dao.*
import com.example.toodo.data.local.db.entity.*
import com.example.toodo.data.mapper.toDomain
import com.example.toodo.data.mapper.toEntity
import com.example.toodo.domain.model.*
import com.example.toodo.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val subTaskDao: SubTaskDao,
    private val taskListDao: TaskListDao,
    private val recurrenceRuleDao: RecurrenceRuleDao,
    private val reminderConfigDao: ReminderConfigDao,
    private val completionRecordDao: CompletionRecordDao
) : TaskRepository {

    override fun getTodayTasks(todayEpochDay: Long): Flow<List<Task>> {
        val todayDate = LocalDate.ofEpochDay(todayEpochDay)
        val todayStartMillis = todayDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEndMillis = todayDate.plusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        val todayDayOfWeek = todayDate.dayOfWeek

        return combine(
            taskDao.getOneTimeTodayTasks(todayEndMillis),
            taskDao.getDailyTasks(),
            taskDao.getRecurringTasks(),
            completionRecordDao.getByDate(todayEpochDay),
            taskDao.getCompletedTodayTasks(todayStartMillis, todayEndMillis)
        ) { oneTimeEntities, dailyEntities, recurringEntities, todayCompletions, completedTodayEntities ->
            val completedTaskIds = todayCompletions.map { it.taskId }.toSet()

            // Filter daily: exclude those already completed today
            val filteredDaily = dailyEntities.filter { it.id !in completedTaskIds }

            // Filter recurring: exclude completed today AND match day-of-week
            val filteredRecurring = recurringEntities.filter { entity ->
                if (entity.id in completedTaskIds) return@filter false
                matchesToday(entity, todayDayOfWeek)
            }

            // Merge: uncompleted today + completed today
            val allEntities = oneTimeEntities + filteredDaily + filteredRecurring + completedTodayEntities
            allEntities.map { mapEntityToTask(it) }
        }
    }

    override fun getFocusTasks(): Flow<List<Task>> {
        return taskDao.getFocusTasks().map { entities ->
            entities.map { mapEntityToTask(it) }
        }
    }

    override fun searchTasks(query: String): Flow<List<Task>> {
        return taskDao.searchTasks(query).map { entities ->
            entities.map { mapEntityToTask(it) }
        }
    }

    override fun getTasksByList(listId: Long): Flow<List<Task>> {
        return taskDao.getTasksByList(listId).map { entities ->
            entities.map { mapEntityToTask(it) }
        }
    }

    override fun getTasksByDateRange(startMillis: Long, endMillis: Long): Flow<List<Task>> {
        return taskDao.getTasksByDateRange(startMillis, endMillis).map { entities ->
            entities.map { mapEntityToTask(it) }
        }
    }

    override fun getTaskById(id: Long): Flow<Task?> {
        return taskDao.observeById(id).map { entity ->
            entity?.let { mapEntityToTask(it) }
        }
    }

    override suspend fun createTask(
        task: Task,
        recurrenceRule: RecurrenceRule?,
        reminders: List<ReminderConfig>,
        subTasks: List<SubTask>
    ): Long {
        // Save recurrence rule first if present
        val ruleId = recurrenceRule?.let { recurrenceRuleDao.insert(it.toEntity()) }

        // Save the task
        val entity = task.toEntity().copy(recurrenceRuleId = ruleId ?: task.recurrenceRuleId)
        val taskId = taskDao.insert(entity)

        // Save reminders with correct taskId
        if (reminders.isNotEmpty()) {
            reminderConfigDao.insertAll(
                reminders.map { it.toEntity().copy(taskId = taskId) }
            )
        }

        // Save sub-tasks with correct parentId
        subTasks.forEach { subTask ->
            subTaskDao.insert(subTask.toEntity().copy(parentId = taskId))
        }

        return taskId
    }

    override suspend fun updateTask(task: Task) {
        taskDao.update(task.toEntity())
    }

    override suspend fun deleteTask(taskId: Long) {
        taskDao.deleteById(taskId)
    }

    override suspend fun completeTask(taskId: Long, date: Long, isFocus: Boolean) {
        val now = java.time.Instant.now().toEpochMilli()
        taskDao.updateCompletedAt(taskId, now)
        completionRecordDao.insert(
            CompletionRecordEntity(
                taskId = taskId,
                date = date,
                completedAt = now,
                isFocusTask = isFocus
            )
        )
    }

    override suspend fun uncompleteTask(taskId: Long, date: Long) {
        taskDao.updateCompletedAt(taskId, null)
        val record = completionRecordDao.getByTaskAndDate(taskId, date)
        record?.let { completionRecordDao.delete(it) }
    }

    override suspend fun skipTodayTask(taskId: Long, date: Long) {
        // Write CompletionRecord WITHOUT setting completedAt → hides from today view
        // without counting as "completed" for streaks/stats
        val now = java.time.Instant.now().toEpochMilli()
        completionRecordDao.insert(
            CompletionRecordEntity(
                taskId = taskId,
                date = date,
                completedAt = now,
                isFocusTask = false
            )
        )
    }

    override suspend fun updateFocusStatus(taskId: Long, isFocus: Boolean) {
        taskDao.updateFocusStatus(taskId, isFocus)
    }

    override suspend fun updateDueDate(taskId: Long, dueDate: Long?) {
        taskDao.updateDueDate(taskId, dueDate)
    }

    override suspend fun addSubTask(subTask: SubTask): Long {
        return subTaskDao.insert(subTask.toEntity())
    }

    override suspend fun updateSubTask(subTask: SubTask) {
        subTaskDao.update(subTask.toEntity())
    }

    override suspend fun deleteSubTask(subTaskId: Long) {
        val dummy = SubTaskEntity(id = subTaskId, parentId = 0, title = "")
        subTaskDao.delete(dummy)
    }

    override suspend fun addReminderConfig(config: ReminderConfig): Long {
        return reminderConfigDao.insert(config.toEntity())
    }

    override suspend fun deleteRemindersForTask(taskId: Long) {
        reminderConfigDao.deleteByTaskId(taskId)
    }

    override fun getRemindersForTask(taskId: Long): Flow<List<ReminderConfig>> {
        return reminderConfigDao.getByTaskId(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    /** Check if a recurring task matches today's day-of-week based on its recurrence rule. */
    private fun matchesToday(entity: TaskEntity, todayDayOfWeek: java.time.DayOfWeek): Boolean {
        if (entity.taskType != "RECURRING") return true
        val ruleId = entity.recurrenceRuleId ?: return true // no rule = always shows
        val rule = runBlocking { recurrenceRuleDao.getById(ruleId) } ?: return true
        val ruleDomain = rule.toDomain()
        // If rule specifies days, today must match; otherwise show always
        return ruleDomain.byDay.isEmpty() || ruleDomain.byDay.contains(todayDayOfWeek)
    }

    /** Load all related entities and map a TaskEntity to domain Task. */
    private fun mapEntityToTask(entity: TaskEntity): Task {
        val list = entity.listId?.let { runBlocking { taskListDao.getById(it) } }
        val rule = entity.recurrenceRuleId?.let { runBlocking { recurrenceRuleDao.getById(it) } }
        val reminders = runBlocking { reminderConfigDao.getByTaskIdOnce(entity.id) }
        val subTasks = runBlocking { subTaskDao.getByParentIdOnce(entity.id) }
        return entity.toDomain(list, rule, reminders, subTasks)
    }
}
