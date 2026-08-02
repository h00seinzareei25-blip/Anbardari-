package com.focusflow.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.focusflow.app.ui.screens.pomodoro.PomodoroScreen
import com.focusflow.app.ui.screens.stats.StatsScreen
import com.focusflow.app.ui.screens.tasks.TasksScreen
import com.focusflow.app.ui.theme.*

object Routes {
    const val TASKS = "tasks"
    const val POMODORO = "pomodoro?taskId={taskId}"
    const val STATS = "stats"

    fun pomodoro(taskId: Long = -1L) = "pomodoro?taskId=$taskId"
}

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val baseRoute: String
) {
    data object Tasks : Screen(Routes.TASKS, "کارها", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "tasks")
    data object Pomodoro : Screen(Routes.pomodoro(), "پومودورو", Icons.Filled.Timer, Icons.Outlined.Timer, "pomodoro")
    data object Stats : Screen(Routes.STATS, "پیشرفت", Icons.Filled.BarChart, Icons.Outlined.BarChart, "stats")
}

@Composable
fun MainNavGraph(initialTaskId: Long? = null) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Tasks, Screen.Pomodoro, Screen.Stats)

    LaunchedEffect(initialTaskId) {
        if (initialTaskId != null && initialTaskId > 0) {
            navController.navigate(Routes.pomodoro(initialTaskId)) {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDarkCard,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.route?.startsWith(screen.baseRoute) == true
                    } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label, style = MaterialTheme.typography.bodySmall) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            indicatorColor = PrimaryPurple.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        },
        containerColor = SurfaceDark
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.TASKS,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.TASKS) {
                TasksScreen(
                    onStartPomodoro = { taskId ->
                        navController.navigate(Routes.pomodoro(taskId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = Routes.POMODORO,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getLong("taskId")?.takeIf { it > 0 }
                PomodoroScreen(startTaskId = taskId)
            }
            composable(Routes.STATS) { StatsScreen() }
        }
    }
}
