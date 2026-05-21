package com.example.toodo.data.local.db.dao

import androidx.room.*
import com.example.toodo.data.local.db.entity.ReminderConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ReminderConfigEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<ReminderConfigEntity>)

    @Update
    suspend fun update(config: ReminderConfigEntity)

    @Delete
    suspend fun delete(config: ReminderConfigEntity)

    @Query("SELECT * FROM reminder_configs WHERE taskId = :taskId")
    fun getByTaskId(taskId: Long): Flow<List<ReminderConfigEntity>>

    @Query("SELECT * FROM reminder_configs WHERE taskId = :taskId")
    suspend fun getByTaskIdOnce(taskId: Long): List<ReminderConfigEntity>

    @Query("DELETE FROM reminder_configs WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: Long)

    @Query("UPDATE reminder_configs SET lastTriggeredAt = :timestamp WHERE id = :configId")
    suspend fun markTriggered(configId: Long, timestamp: Long)
}
