package com.example.toodo.ui.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Returns a human-friendly relative date string in Chinese:
 * "今天", "明天", "昨天", "M月d日", or "yyyy年M月d日" for past years.
 */
fun LocalDate.toDisplayString(): String {
    val today = LocalDate.now()
    return when {
        this == today -> "今天"
        this == today.plusDays(1) -> "明天"
        this == today.minusDays(1) -> "昨天"
        this.year == today.year -> this.format(DateTimeFormatter.ofPattern("M月d日"))
        else -> this.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))
    }
}

/**
 * Full date with weekday, e.g. "5月21日 星期三".
 */
fun LocalDate.toFullDateString(): String {
    return this.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE))
}

/**
 * Single-character weekday: "一" through "日".
 */
fun DayOfWeek.toChineseShort(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "一"
        DayOfWeek.TUESDAY -> "二"
        DayOfWeek.WEDNESDAY -> "三"
        DayOfWeek.THURSDAY -> "四"
        DayOfWeek.FRIDAY -> "五"
        DayOfWeek.SATURDAY -> "六"
        DayOfWeek.SUNDAY -> "日"
    }
}

/**
 * Full Chinese weekday name, e.g. "星期一" through "星期日".
 */
fun DayOfWeek.toChinese(): String {
    return getDisplayName(TextStyle.FULL, Locale.CHINESE)
}

/**
 * Formats a [LocalTime] as "HH:mm".
 */
fun LocalTime.toDisplayString(): String {
    return this.format(DateTimeFormatter.ofPattern("HH:mm"))
}

/**
 * Converts an epoch day (Long) back to [LocalDate].
 */
fun Long.epochDayToLocalDate(): LocalDate {
    return LocalDate.ofEpochDay(this)
}

/**
 * Converts epoch millis (Long) to [LocalDate] by dividing by ms-per-day.
 */
fun Long.epochMillisToLocalDate(): LocalDate {
    return LocalDate.ofEpochDay(this / (24 * 60 * 60 * 1000))
}
