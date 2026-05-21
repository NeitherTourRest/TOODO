package com.example.toodo.data.local.db.dao

import androidx.room.*
import com.example.toodo.data.local.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<TaskEntity?>

    // One-time tasks due today (not completed)
    @Query("SELECT * FROM tasks WHERE taskType = 'ONE_TIME' AND dueDate IS NOT NULL AND dueDate <= :todayEndMillis AND completedAt IS NULL AND isArchived = 0 ORDER BY priority ASC, sortOrder ASC")
    fun getOneTimeTodayTasks(todayEndMillis: Long): Flow<List<TaskEntity>>

    // All daily tasks (not archived)
    @Query("SELECT * FROM tasks WHERE taskType = 'DAILY' AND isArchived = 0 ORDER BY priority ASC, sortOrder ASC")
    fun getDailyTasks(): Flow<List<TaskEntity>>

    // All recurring tasks (not archived)
    @Query("SELECT * FROM tasks WHERE taskType = 'RECURRING' AND isArchived = 0 ORDER BY priority ASC, sortOrder ASC")
    fun getRecurringTasks(): Flow<List<TaskEntity>>

    // Focus tasks (not completed, not archived)
    @Query("SELECT * FROM tasks WHERE isFocusTask = 1 AND completedAt IS NULL AND isArchived = 0 ORDER BY priority ASC, sortOrder ASC")
    fun getFocusTasks(): Flow<List<TaskEntity>>

    // All incomplete tasks (for search)
    @Query("SELECT * FROM tasks WHERE completedAt IS NULL AND isArchived = 0 AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY priority ASC, dueDate ASC")
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    // Tasks by list/category
    @Query("SELECT * FROM tasks WHERE listId = :listId AND isArchived = 0 ORDER BY priority ASC, sortOrder ASC")
    fun getTasksByList(listId: Long): Flow<List<TaskEntity>>

    // Tasks in a date range (for plan view)
    @Query("SELECT * FROM tasks WHERE dueDate >= :startMillis AND dueDate <= :endMillis AND completedAt IS NULL AND isArchived = 0 ORDER BY dueDate ASC, priority ASC")
    fun getTasksByDateRange(startMillis: Long, endMillis: Long): Flow<List<TaskEntity>>

    // All incomplete tasks with reminders (for rescheduling after boot)
    @Query("SELECT * FROM tasks WHERE completedAt IS NULL AND isArchived = 0 AND taskType = 'ONE_TIME' AND dueDate IS NOT NULL")
    fun getIncompleteTasksWithReminders(): Flow<List<TaskEntity>>

    // Tasks completed today (for showing in completed section)
    @Query("SELECT * FROM tasks WHERE completedAt >= :todayStartMillis AND completedAt <= :todayEndMillis AND isArchived = 0 ORDER BY completedAt DESC")
    fun getCompletedTodayTasks(todayStartMillis: Long, todayEndMillis: Long): Flow<List<TaskEntity>>

    // Update focus status
    @Query("UPDATE tasks SET isFocusTask = :isFocus WHERE id = :taskId")
    suspend fun updateFocusStatus(taskId: Long, isFocus: Boolean)

    // Update sort order
    @Query("UPDATE tasks SET sortOrder = :sortOrder WHERE id = :taskId")
    suspend fun updateSortOrder(taskId: Long, sortOrder: Int)

    // Update completion
    @Query("UPDATE tasks SET completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateCompletedAt(taskId: Long, completedAt: Long?)

    // Update due date
    @Query("UPDATE tasks SET dueDate = :dueDate WHERE id = :taskId")
    suspend fun updateDueDate(taskId: Long, dueDate: Long?)
}
