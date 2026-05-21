package com.example.toodo.domain.usecase

import com.example.toodo.domain.repository.StatsRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetStreakUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(): StreakInfo {
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()

        // Get freezes for the current month
        val currentMonth = YearMonth.from(today)
        val monthStart = currentMonth.atDay(1).toEpochDay()
        val monthEnd = currentMonth.atEndOfMonth().toEpochDay()
        val freezesUsed = statsRepository.getFreezesUsedThisMonth(monthStart, monthEnd)
        val freezesRemaining = maxOf(0, 2 - freezesUsed)

        // Pre-fetch all freeze dates in the past year for streak calculation
        val allFreezes = statsRepository.getFreezesBetween(todayEpochDay - 365, todayEpochDay)
        val freezeDates = allFreezes.map { it.date }.toSet()

        // Determine starting point for streak calculation
        val todayCompleted = statsRepository.getCompletionCountForDate(todayEpochDay) > 0
        var date = if (!todayCompleted && todayEpochDay !in freezeDates) {
            todayEpochDay - 1 // Today not completed and no freeze → start from yesterday
        } else {
            todayEpochDay
        }

        // Calculate current streak by walking backwards
        var currentStreak = 0
        while (date >= todayEpochDay - 365) {
            val hasCompletion = statsRepository.getCompletionCountForDate(date) > 0
            val hasFreeze = date in freezeDates

            if (hasCompletion || hasFreeze) {
                currentStreak++
                date -= 1
            } else {
                break
            }
        }

        // Walk further back to find the longest streak in the past year
        var longestStreak = currentStreak
        var tempStreak = 0
        var scanDate = date // Continue from where we broke

        while (scanDate >= todayEpochDay - 365) {
            val hasCompletion = statsRepository.getCompletionCountForDate(scanDate) > 0
            val hasFreeze = scanDate in freezeDates

            if (hasCompletion || hasFreeze) {
                tempStreak++
            } else {
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak
                }
                tempStreak = 0
            }
            scanDate -= 1
        }
        // Check final segment
        if (tempStreak > longestStreak) {
            longestStreak = tempStreak
        }

        return StreakInfo(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            freezesRemaining = freezesRemaining
        )
    }

    data class StreakInfo(
        val currentStreak: Int,
        val longestStreak: Int,
        val freezesRemaining: Int
    )
}
