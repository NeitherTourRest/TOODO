package com.example.toodo.domain.usecase

import com.example.toodo.domain.model.Frequency
import com.example.toodo.domain.model.RecurrenceRule
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class CalculateNextOccurrenceUseCase @Inject constructor() {

    operator fun invoke(rule: RecurrenceRule, fromDate: LocalDate = LocalDate.now()): LocalDate? {
        val base = if (fromDate.isAfter(LocalDate.now())) fromDate else LocalDate.now()

        val nextDate = when (rule.freq) {
            Frequency.DAILY -> {
                if (rule.interval == 1) {
                    base.plusDays(1)
                } else {
                    generateSequence(base.plusDays(1)) { it.plusDays(1) }
                        .first { daysBetween(fromDate, it) % rule.interval == 0L }
                }
            }
            Frequency.WEEKLY -> {
                if (rule.byDay.isEmpty()) {
                    base.plusWeeks(rule.interval.toLong())
                } else {
                    generateSequence(base.plusDays(1)) { it.plusDays(1) }
                        .first { it.dayOfWeek in rule.byDay }
                }
            }
            Frequency.MONTHLY -> {
                if (rule.byMonthDay.isEmpty()) {
                    base.plusMonths(rule.interval.toLong())
                } else {
                    generateSequence(base.plusDays(1)) { it.plusDays(1) }
                        .first {
                            it.dayOfMonth in rule.byMonthDay &&
                                    it.month == base.plusMonths(rule.interval.toLong()).month
                        }
                }
            }
        }

        // Check against count and untilDate limits
        if (rule.untilDate != null && nextDate.isAfter(rule.untilDate)) {
            return null
        }
        return nextDate
    }

    private fun daysBetween(start: LocalDate, end: LocalDate): Long {
        return end.toEpochDay() - start.toEpochDay()
    }

    fun matchesDate(rule: RecurrenceRule, date: LocalDate): Boolean {
        return when (rule.freq) {
            Frequency.DAILY -> {
                if (rule.interval == 1) true
                else date.toEpochDay() % rule.interval == 0L
            }
            Frequency.WEEKLY -> {
                if (rule.byDay.isEmpty()) true
                else date.dayOfWeek in rule.byDay
            }
            Frequency.MONTHLY -> {
                if (rule.byMonthDay.isEmpty()) true
                else date.dayOfMonth in rule.byMonthDay
            }
        }
    }
}
