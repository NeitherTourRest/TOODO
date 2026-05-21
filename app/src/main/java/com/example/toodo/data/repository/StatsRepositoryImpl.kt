package com.example.toodo.data.repository

import com.example.toodo.data.local.db.dao.CompletionRecordDao
import com.example.toodo.data.local.db.entity.StreakFreezeEntity
import com.example.toodo.data.mapper.toDomain
import com.example.toodo.domain.model.CompletionRecord
import com.example.toodo.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val completionRecordDao: CompletionRecordDao
) : StatsRepository {

    override fun getCompletionRecordsBetween(startDate: Long, endDate: Long): Flow<List<CompletionRecord>> {
        return completionRecordDao.getByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCompletionCountForDate(date: Long): Int {
        return completionRecordDao.getCompletionCountForDate(date)
    }

    override suspend fun getFocusCompletionCountForDate(date: Long): Int {
        return completionRecordDao.getFocusCompletionCountForDate(date)
    }

    override suspend fun getFreezesUsedThisMonth(monthStart: Long, monthEnd: Long): Int {
        return completionRecordDao.getFreezesUsedThisMonth(monthStart, monthEnd)
    }

    override suspend fun useFreeze(date: Long) {
        completionRecordDao.insertFreeze(StreakFreezeEntity(date = date))
    }

    override suspend fun removeFreeze(date: Long) {
        val freezes = completionRecordDao.getFreezesBetween(date, date)
        freezes.forEach { completionRecordDao.deleteFreeze(it) }
    }

    override fun getFreezesBetween(startDate: Long, endDate: Long): List<StreakFreezeEntity> {
        // Non-suspend wrapper — delegates to the suspend version via runBlocking
        return kotlinx.coroutines.runBlocking {
            completionRecordDao.getFreezesBetween(startDate, endDate)
        }
    }
}
