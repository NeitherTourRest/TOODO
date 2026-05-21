package com.example.toodo.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminder_configs",
    foreignKeys = [
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"],
            childColumns = ["taskId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("taskId")]
)
data class ReminderConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val offsetMinutes: Long,
    val isEnabled: Boolean = true,
    val lastTriggeredAt: Long? = null
)
