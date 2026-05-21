package com.example.toodo.data.mapper

import com.example.toodo.data.local.db.entity.CompletionRecordEntity
import com.example.toodo.data.local.db.entity.RecurrenceRuleEntity
import com.example.toodo.data.local.db.entity.ReminderConfigEntity
import com.example.toodo.data.local.db.entity.SubTaskEntity
import com.example.toodo.data.local.db.entity.TaskEntity
import com.example.toodo.data.local.db.entity.TaskListEntity
import com.example.toodo.domain.model.CompletionRecord
import com.example.toodo.domain.model.Frequency
import com.example.toodo.domain.model.Priority
import com.example.toodo.domain.model.RecurrenceRule
import com.example.toodo.domain.model.ReminderConfig
import com.example.toodo.domain.model.SubTask
import com.example.toodo.domain.model.Task
import com.example.toodo.domain.model.TaskList
import com.example.toodo.domain.model.TaskType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

// ─── TaskListEntity <-> TaskList ────────────────────────────────────────────

fun TaskListEntity.toDomain() = TaskList(
    id = id,
    name = name,
    iconName = iconName,
    color = color,
    sortOrder = sortOrder
)

fun TaskList.toEntity() = TaskListEntity(
    id = id,
    name = name,
    iconName = iconName,
    color = color,
    sortOrder = sortOrder
)

// ─── RecurrenceRuleEntity <-> RecurrenceRule ────────────────────────────────

fun RecurrenceRuleEntity.toDomain() = RecurrenceRule(
    id = id,
    freq = try { Frequency.valueOf(freq) } catch (_: Exception) { Frequency.DAILY },
    interval = interval,
    byDay = parseDayOfWeekSet(byDay),
    byMonthDay = parseIntSet(byMonthDay),
    count = count,
    untilDate = untilDate?.let { LocalDate.ofEpochDay(it) }
)

fun RecurrenceRule.toEntity() = RecurrenceRuleEntity(
    id = id,
    freq = freq.name,
    interval = interval,
    byDay = byDay.takeIf { it.isNotEmpty() }?.joinToString(",") { it.name },
    byMonthDay = byMonthDay.takeIf { it.isNotEmpty() }?.joinToString(","),
    count = count,
    untilDate = untilDate?.toEpochDay()
)

// ─── ReminderConfigEntity <-> ReminderConfig ────────────────────────────────

fun ReminderConfigEntity.toDomain() = ReminderConfig(
    id = id,
    taskId = taskId,
    offsetMinutes = offsetMinutes,
    isEnabled = isEnabled
)

fun ReminderConfig.toEntity() = ReminderConfigEntity(
    id = id,
    taskId = taskId,
    offsetMinutes = offsetMinutes,
    isEnabled = isEnabled
)

// ─── SubTaskEntity <-> SubTask ──────────────────────────────────────────────

fun SubTaskEntity.toDomain() = SubTask(
    id = id,
    parentId = parentId,
    title = title,
    isCompleted = isCompleted,
    sortOrder = sortOrder
)

fun SubTask.toEntity() = SubTaskEntity(
    id = id,
    parentId = parentId,
    title = title,
    isCompleted = isCompleted,
    sortOrder = sortOrder
)

// ─── CompletionRecordEntity <-> CompletionRecord ────────────────────────────

fun CompletionRecordEntity.toDomain() = CompletionRecord(
    id = id,
    taskId = taskId,
    date = date,
    completedAt = completedAt,
    isFocusTask = isFocusTask
)

fun CompletionRecord.toEntity() = CompletionRecordEntity(
    id = id,
    taskId = taskId,
    date = date,
    completedAt = completedAt,
    isFocusTask = isFocusTask
)

// ─── TaskEntity -> Task (with related entities) ─────────────────────────────

fun TaskEntity.toDomain(
    list: TaskListEntity? = null,
    recurrenceRule: RecurrenceRuleEntity? = null,
    reminders: List<ReminderConfigEntity> = emptyList(),
    subTasks: List<SubTaskEntity> = emptyList()
): Task = Task(
    id = id,
    title = title,
    description = description,
    taskType = try { TaskType.valueOf(taskType) } catch (_: Exception) { TaskType.ONE_TIME },
    priority = Priority.entries.firstOrNull { it.value == priority } ?: Priority.MEDIUM,
    listId = listId,
    list = list?.toDomain(),
    dueDate = dueDate?.let { LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000)) },
    dueTime = dueTime?.let { LocalTime.ofSecondOfDay(it.toLong() * 60) },
    recurrenceRuleId = recurrenceRuleId,
    recurrenceRule = recurrenceRule?.toDomain(),
    reminders = reminders.map { it.toDomain() },
    subTasks = subTasks.map { it.toDomain() },
    isFocusTask = isFocusTask,
    isArchived = isArchived,
    createdAt = Instant.ofEpochMilli(createdAt),
    completedAt = completedAt?.let { Instant.ofEpochMilli(it) },
    sortOrder = sortOrder
)

// ─── Task -> TaskEntity ─────────────────────────────────────────────────────

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    taskType = taskType.name,
    priority = priority.value,
    listId = listId,
    dueDate = dueDate?.toEpochDay()?.times(24 * 60 * 60 * 1000),
    dueTime = dueTime?.toSecondOfDay()?.div(60),
    recurrenceRuleId = recurrenceRuleId,
    isFocusTask = isFocusTask,
    isArchived = isArchived,
    createdAt = createdAt.toEpochMilli(),
    completedAt = completedAt?.toEpochMilli(),
    sortOrder = sortOrder
)

// ─── Private helpers ────────────────────────────────────────────────────────

private fun parseDayOfWeekSet(raw: String?): Set<DayOfWeek> {
    if (raw.isNullOrBlank()) return emptySet()
    return raw.split(",").mapNotNull { part ->
        runCatching { DayOfWeek.valueOf(part.trim()) }.getOrNull()
    }.toSet()
}

private fun parseIntSet(raw: String?): Set<Int> {
    if (raw.isNullOrBlank()) return emptySet()
    return raw.split(",").mapNotNull { part ->
        part.trim().toIntOrNull()
    }.toSet()
}
