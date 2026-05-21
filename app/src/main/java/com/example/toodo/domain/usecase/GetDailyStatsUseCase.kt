package com.example.toodo.domain.usecase

import com.example.toodo.domain.repository.StatsRepository
import java.time.LocalDate
import javax.inject.Inject

class GetDailyStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): DailyStats {
        val epochDay = date.toEpochDay()
        val completedCount = statsRepository.getCompletionCountForDate(epochDay)
        val focusCount = statsRepository.getFocusCompletionCountForDate(epochDay)
        return DailyStats(date, completedCount, focusCount)
    }

    data class DailyStats(
        val date: LocalDate,
        val completedCount: Int,
        val focusCompletedCount: Int
    )
}
