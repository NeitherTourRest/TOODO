package com.example.toodo.data.repository

import com.example.toodo.data.local.db.dao.TaskListDao
import com.example.toodo.data.mapper.toDomain
import com.example.toodo.data.mapper.toEntity
import com.example.toodo.domain.model.TaskList
import com.example.toodo.domain.repository.TaskListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskListRepositoryImpl @Inject constructor(
    private val taskListDao: TaskListDao
) : TaskListRepository {

    override fun getAllLists(): Flow<List<TaskList>> {
        return taskListDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getListById(id: Long): TaskList? {
        return taskListDao.getById(id)?.toDomain()
    }

    override suspend fun createList(taskList: TaskList): Long {
        return taskListDao.insert(taskList.toEntity())
    }

    override suspend fun updateList(taskList: TaskList) {
        taskListDao.update(taskList.toEntity())
    }

    override suspend fun deleteList(taskList: TaskList) {
        taskListDao.delete(taskList.toEntity())
    }
}
