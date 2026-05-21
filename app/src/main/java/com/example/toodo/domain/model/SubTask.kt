package com.example.toodo.domain.model

data class SubTask(
    val id: Long = 0,
    val parentId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0
)
