package com.example.toodo.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val taskType: TaskType = TaskType.ONE_TIME,
    val priority: Priority = Priority.MEDIUM,
    val listId: Long? = null,
    val list: TaskList? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val recurrenceRuleId: Long? = null,
    val recurrenceRule: RecurrenceRule? = null,
    val reminders: List<ReminderConfig> = emptyList(),
    val subTasks: List<SubTask> = emptyList(),
    val isFocusTask: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null,
    val sortOrder: Int = 0
) {
    val isCompleted: Boolean get() = completedAt != null
    val isOverdue: Boolean get() = dueDate != null && !isCompleted && dueDate < LocalDate.now()
    val subTaskProgress: Float get() = if (subTasks.isEmpty()) -1f else subTasks.count { it.isCompleted }.toFloat() / subTasks.size
}
