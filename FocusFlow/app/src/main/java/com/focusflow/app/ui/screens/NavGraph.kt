package com.focusflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusflow.app.ui.screens.pomodoro.PomodoroScreen
import com.focusflow.app.ui.screens.stats.StatsScreen
import com.focusflow.app.ui.screens.tasks.TasksScreen
import com.focusflow.app.ui.theme.*

sealed class Screen(val route: String, val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Tasks : Screen("tasks", "کارها", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
    object Pomodoro : Screen("pomodoro", "پومودورو", Icons.Filled.Timer, Icons.Outlined.Timer)
    object Stats : Screen("stats", "پیشرفت", Icons.Filled.BarChart, Icons.Outlined.BarChart)
}

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Tasks, Screen.Pomodoro, Screen.Stats)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDarkCard,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
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
            startDestination = Screen.Tasks.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Tasks.route) { TasksScreen() }
            composable(Screen.Pomodoro.route) { PomodoroScreen() }
            composable(Screen.Stats.route) { StatsScreen() }
        }
    }
}
