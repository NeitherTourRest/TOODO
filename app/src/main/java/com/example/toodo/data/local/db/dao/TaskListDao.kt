package com.example.toodo.data.local.db.dao

import androidx.room.*
import com.example.toodo.data.local.db.entity.TaskListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskList: TaskListEntity): Long

    @Update
    suspend fun update(taskList: TaskListEntity)

    @Delete
    suspend fun delete(taskList: TaskListEntity)

    @Query("SELECT * FROM task_lists ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists WHERE id = :id")
    suspend fun getById(id: Long): TaskListEntity?

    @Query("SELECT * FROM task_lists WHERE id = :id")
    fun observeById(id: Long): Flow<TaskListEntity?>
}
