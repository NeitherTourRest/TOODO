package com.example.toodo.domain.model

data class CompletionRecord(
    val id: Long = 0,
    val taskId: Long,
    val date: Long,
    val completedAt: Long,
    val isFocusTask: Boolean = false
)
