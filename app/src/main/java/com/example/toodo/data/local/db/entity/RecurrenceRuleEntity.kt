package com.example.toodo.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recurrence_rules", indices = [Index(value = ["id"])])
data class RecurrenceRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val freq: String,
    val interval: Int = 1,
    val byDay: String? = null,
    val byMonthDay: String? = null,
    val count: Int? = null,
    val untilDate: Long? = null
)
