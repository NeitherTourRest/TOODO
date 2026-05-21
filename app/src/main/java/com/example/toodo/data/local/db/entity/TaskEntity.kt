package com.example.toodo.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(entity = TaskListEntity::class, parentColumns = ["id"],
            childColumns = ["listId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = RecurrenceRuleEntity::class, parentColumns = ["id"],
            childColumns = ["recurrenceRuleId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("listId"), Index("recurrenceRuleId"), Index("dueDate"), Index("completedAt")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val taskType: String = "ONE_TIME",
    val priority: Int = 2,
    val listId: Long? = null,
    val dueDate: Long? = null,
    val dueTime: Int? = null,
    val recurrenceRuleId: Long? = null,
    val isFocusTask: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val sortOrder: Int = 0
)
