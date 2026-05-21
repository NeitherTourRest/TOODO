package com.example.toodo.domain.model

data class ReminderConfig(
    val id: Long = 0,
    val taskId: Long = 0,
    val offsetMinutes: Long,
    val isEnabled: Boolean = true
)
