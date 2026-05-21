package com.example.toodo.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "streak_freezes", indices = [Index(value = ["date"], unique = true)])
data class StreakFreezeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long
)
