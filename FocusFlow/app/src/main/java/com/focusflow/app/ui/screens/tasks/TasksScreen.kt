package com.focusflow.app.ui.screens.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.app.ui.components.TaskCard
import com.focusflow.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: TaskViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "کارهام",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            "${uiState.activeTasks.size} کار باقی‌مانده",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::toggleShowCompleted) {
                        Text(
                            if (uiState.showCompleted) "پنهان کردن" else "انجام‌شده‌ها",
                            color = PrimaryPurpleLight,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )

            if (uiState.activeTasks.isEmpty() && !uiState.showCompleted) {
                EmptyTasksView(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
                ) {
                    if (uiState.activeTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "کارهای فعال",
                                count = uiState.activeTasks.size
                            )
                        }
                        items(uiState.activeTasks, key = { it.id }) { task ->
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInHorizontally() + fadeIn(),
                                exit = slideOutHorizontally() + fadeOut()
                            ) {
                                TaskCard(
                                    task = task,
                                    onComplete = { viewModel.completeTask(task) },
                                    onDelete = { viewModel.deleteTask(task) }
                                )
                            }
                        }
                    }

                    if (uiState.showCompleted && uiState.completedTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "انجام‌شده‌ها ✓",
                                count = uiState.completedTasks.size
                            )
                        }
                        items(uiState.completedTasks, key = { "done_${it.id}" }) { task ->
                            TaskCard(
                                task = task,
                                onComplete = {},
                                onDelete = { viewModel.deleteTask(task) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = PrimaryPurple,
            contentColor = androidx.compose.ui.graphics.Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "افزودن کار", modifier = Modifier.size(28.dp))
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            containerColor = SurfaceDarkCard,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AddTaskSheet(
                onDismiss = { showAddSheet = false },
                onAdd = { title, desc, priority, category, dueDate, pomodoros ->
                    viewModel.addTask(title, desc, priority, category, dueDate, pomodoros)
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextSecondary)
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("$count", style = MaterialTheme.typography.bodySmall, color = PrimaryPurpleLight)
        }
    }
}

@Composable
private fun EmptyTasksView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "هیچ کاری نداری!",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "یه کار جدید اضافه کن\nو شروع کن به پیشرفت",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
