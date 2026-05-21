package com.example.toodo.data.local.db.dao

import androidx.room.*
import com.example.toodo.data.local.db.entity.SubTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subTask: SubTaskEntity): Long

    @Update
    suspend fun update(subTask: SubTaskEntity)

    @Delete
    suspend fun delete(subTask: SubTaskEntity)

    @Query("SELECT * FROM sub_tasks WHERE parentId = :parentId ORDER BY sortOrder ASC")
    fun getByParentId(parentId: Long): Flow<List<SubTaskEntity>>

    @Query("SELECT * FROM sub_tasks WHERE parentId = :parentId ORDER BY sortOrder ASC")
    suspend fun getByParentIdOnce(parentId: Long): List<SubTaskEntity>

    @Query("UPDATE sub_tasks SET isCompleted = :isCompleted WHERE id = :subTaskId")
    suspend fun updateCompleted(subTaskId: Long, isCompleted: Boolean)

    @Query("DELETE FROM sub_tasks WHERE parentId = :parentId")
    suspend fun deleteByParentId(parentId: Long)
}
