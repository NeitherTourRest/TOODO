package com.example.toodo.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class RecurrenceRule(
    val id: Long = 0,
    val freq: Frequency,
    val interval: Int = 1,
    val byDay: Set<DayOfWeek> = emptySet(),
    val byMonthDay: Set<Int> = emptySet(),
    val count: Int? = null,
    val untilDate: LocalDate? = null
)

enum class Frequency { DAILY, WEEKLY, MONTHLY }
