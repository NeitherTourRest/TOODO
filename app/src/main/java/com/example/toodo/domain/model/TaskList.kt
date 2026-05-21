package com.example.toodo.domain.model

data class TaskList(
    val id: Long = 0,
    val name: String,
    val iconName: String = "folder",
    val color: Long = 0xFF6750A4,
    val sortOrder: Int = 0
)
