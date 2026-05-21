package com.example.toodo.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object TodayRoute
@Serializable object PlanRoute
@Serializable object StatsRoute
@Serializable object SettingsRoute
@Serializable data class AddTaskRoute(val taskId: Long? = null)
@Serializable data class TaskDetailRoute(val taskId: Long)
@Serializable object ManageListsRoute
@Serializable data class TaskListRoute(val listId: Long)
