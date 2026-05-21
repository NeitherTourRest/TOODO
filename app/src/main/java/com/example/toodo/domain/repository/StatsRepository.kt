package com.example.toodo.domain.repository

import com.example.toodo.data.local.db.entity.StreakFreezeEntity
import com.example.toodo.domain.model.CompletionRecord
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun getCompletionRecordsBetween(startDate: Long, endDate: Long): Flow<List<CompletionRecord>>
    suspend fun getCompletionCountForDate(date: Long): Int
    suspend fun getFocusCompletionCountForDate(date: Long): Int
    suspend fun getFreezesUsedThisMonth(monthStart: Long, monthEnd: Long): Int
    suspend fun useFreeze(date: Long)
    suspend fun removeFreeze(date: Long)
    fun getFreezesBetween(startDate: Long, endDate: Long): List<StreakFreezeEntity>
}
