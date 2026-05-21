package com.example.toodo.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completion_records",
    indices = [
        Index(value = ["taskId", "date"], unique = true),
        Index(value = ["date"])
    ]
)
data class CompletionRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val date: Long,
    val completedAt: Long,
    val isFocusTask: Boolean = false
)
