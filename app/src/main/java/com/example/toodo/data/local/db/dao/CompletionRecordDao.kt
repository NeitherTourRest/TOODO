package com.example.toodo.data.local.db.dao

import androidx.room.*
import com.example.toodo.data.local.db.entity.CompletionRecordEntity
import com.example.toodo.data.local.db.entity.StreakFreezeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CompletionRecordEntity): Long

    @Delete
    suspend fun delete(record: CompletionRecordEntity)

    @Query("SELECT * FROM completion_records WHERE taskId = :taskId AND date = :date LIMIT 1")
    suspend fun getByTaskAndDate(taskId: Long, date: Long): CompletionRecordEntity?

    @Query("SELECT * FROM completion_records WHERE date = :date")
    fun getByDate(date: Long): Flow<List<CompletionRecordEntity>>

    @Query("SELECT * FROM completion_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<CompletionRecordEntity>>

    @Query("SELECT COUNT(*) FROM completion_records WHERE date = :date")
    suspend fun getCompletionCountForDate(date: Long): Int

    @Query("SELECT COUNT(*) FROM completion_records WHERE date = :date AND isFocusTask = 1")
    suspend fun getFocusCompletionCountForDate(date: Long): Int

    @Query("DELETE FROM completion_records WHERE taskId = :taskId AND date = :date")
    suspend fun deleteByTaskAndDate(taskId: Long, date: Long)

    // Streak freeze operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFreeze(freeze: StreakFreezeEntity): Long

    @Query("SELECT * FROM streak_freezes WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getFreezesBetween(startDate: Long, endDate: Long): List<StreakFreezeEntity>

    @Query("SELECT COUNT(*) FROM streak_freezes WHERE date >= :monthStart AND date <= :monthEnd")
    suspend fun getFreezesUsedThisMonth(monthStart: Long, monthEnd: Long): Int

    @Delete
    suspend fun deleteFreeze(freeze: StreakFreezeEntity)
}
