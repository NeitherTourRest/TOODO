package com.example.toodo.data.local.db.converter

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toTimestamp(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromMinuteOfDay(value: Int?): LocalTime? = value?.let { LocalTime.ofSecondOfDay(it.toLong() * 60) }

    @TypeConverter
    fun toMinuteOfDay(time: LocalTime?): Int? = time?.toSecondOfDay()?.div(60)

    @TypeConverter
    fun fromDayOfWeekSet(value: String?): Set<DayOfWeek> =
        value?.split(",")?.mapNotNull { runCatching { DayOfWeek.valueOf(it.trim()) }.getOrNull() }?.toSet() ?: emptySet()

    @TypeConverter
    fun toDayOfWeekString(days: Set<DayOfWeek>?): String? =
        days?.joinToString(",") { it.name }
}
