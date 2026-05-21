package com.example.toodo.data.local.db.dao

import androidx.room.*
import com.example.toodo.data.local.db.entity.RecurrenceRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurrenceRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RecurrenceRuleEntity): Long

    @Update
    suspend fun update(rule: RecurrenceRuleEntity)

    @Delete
    suspend fun delete(rule: RecurrenceRuleEntity)

    @Query("SELECT * FROM recurrence_rules WHERE id = :id")
    suspend fun getById(id: Long): RecurrenceRuleEntity?

    @Query("SELECT * FROM recurrence_rules")
    fun getAll(): Flow<List<RecurrenceRuleEntity>>

    @Query("SELECT * FROM recurrence_rules WHERE freq = :freq")
    fun getByFreq(freq: String): Flow<List<RecurrenceRuleEntity>>

    @Query("DELETE FROM recurrence_rules WHERE id = :id")
    suspend fun deleteById(id: Long)
}
