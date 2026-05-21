package com.example.toodo.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.toodo.ui.screen.today.TodayScreen
import com.example.toodo.ui.screen.plan.PlanScreen
import com.example.toodo.ui.screen.stats.StatsScreen
import com.example.toodo.ui.screen.settings.SettingsScreen
import com.example.toodo.ui.screen.addtask.AddTaskScreen
import com.example.toodo.ui.screen.taskdetail.TaskDetailScreen
import com.example.toodo.ui.screen.managelists.ManageListsScreen
import com.example.toodo.ui.screen.tasklist.TaskListScreen

data class BottomNavItem(
    val route: Any,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(TodayRoute, "今日", Icons.Default.CalendarToday),
    BottomNavItem(PlanRoute, "计划", Icons.Default.ViewAgenda),
    BottomNavItem(StatsRoute, "统计", Icons.Default.BarChart),
    BottomNavItem(SettingsRoute, "设置", Icons.Default.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToodoNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.route::class.qualifiedName } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == item.route::class.qualifiedName
                            } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TodayRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<TodayRoute> {
                TodayScreen(
                    onAddTask = { navController.navigate(AddTaskRoute()) },
                    onTaskClick = { taskId -> navController.navigate(TaskDetailRoute(taskId)) }
                )
            }
            composable<PlanRoute> {
                PlanScreen(
                    onTaskClick = { taskId -> navController.navigate(TaskDetailRoute(taskId)) },
                    onAddTask = { navController.navigate(AddTaskRoute()) }
                )
            }
            composable<StatsRoute> {
                StatsScreen()
            }
            composable<SettingsRoute> {
                SettingsScreen(
                    onManageLists = { navController.navigate(ManageListsRoute) }
                )
            }
            composable<AddTaskRoute> {
                AddTaskScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<TaskDetailRoute> {
                TaskDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<ManageListsRoute> {
                ManageListsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<TaskListRoute> {
                TaskListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTaskClick = { taskId -> navController.navigate(TaskDetailRoute(taskId)) }
                )
            }
        }
    }
}
