package com.example.toodo.domain.repository

import com.example.toodo.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

interface TaskListRepository {
    fun getAllLists(): Flow<List<TaskList>>
    suspend fun getListById(id: Long): TaskList?
    suspend fun createList(taskList: TaskList): Long
    suspend fun updateList(taskList: TaskList)
    suspend fun deleteList(taskList: TaskList)
}
