package com.example.toodo.domain.repository

import com.example.toodo.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTodayTasks(todayEpochDay: Long): Flow<List<Task>>
    fun getFocusTasks(): Flow<List<Task>>
    fun searchTasks(query: String): Flow<List<Task>>
    fun getTasksByList(listId: Long): Flow<List<Task>>
    fun getTasksByDateRange(startMillis: Long, endMillis: Long): Flow<List<Task>>
    fun getTaskById(id: Long): Flow<Task?>
    suspend fun createTask(task: Task, recurrenceRule: RecurrenceRule?, reminders: List<ReminderConfig>, subTasks: List<SubTask>): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: Long)
    suspend fun completeTask(taskId: Long, date: Long, isFocus: Boolean)
    suspend fun uncompleteTask(taskId: Long, date: Long)
    suspend fun skipTodayTask(taskId: Long, date: Long)
    suspend fun updateFocusStatus(taskId: Long, isFocus: Boolean)
    suspend fun updateDueDate(taskId: Long, dueDate: Long?)
    suspend fun addSubTask(subTask: SubTask): Long
    suspend fun updateSubTask(subTask: SubTask)
    suspend fun deleteSubTask(subTaskId: Long)
    suspend fun addReminderConfig(config: ReminderConfig): Long
    suspend fun deleteRemindersForTask(taskId: Long)
    fun getRemindersForTask(taskId: Long): Flow<List<ReminderConfig>>
}
