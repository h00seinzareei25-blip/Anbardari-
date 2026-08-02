package com.focusflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.model.Priority
import com.focusflow.app.domain.model.Task
import com.focusflow.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TaskCard(
    task: Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onStartPomodoro: (() -> Unit)? = null,
    isCommitted: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isOverdue = task.dueDate != null && task.dueDate.isBefore(LocalDate.now()) && !task.isCompleted

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCommitted) PrimaryPurple.copy(alpha = 0.15f) else CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PriorityIndicator(priority = task.priority)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCommitted) {
                        Text("🎯 ", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (task.isCompleted) TextSecondary else TextPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextHint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryChip(category = task.category.emoji + " " + task.category.label)

                    if (task.dueDate != null) {
                        DueDateChip(date = task.dueDate, isOverdue = isOverdue)
                    }

                    if (task.estimatedPomodoros > 0) {
                        PomodoroChip(
                            completed = task.completedPomodoros,
                            estimated = task.estimatedPomodoros
                        )
                    }
                }
            }

            if (!task.isCompleted) {
                Spacer(modifier = Modifier.width(4.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (onStartPomodoro != null) {
                        IconButton(
                            onClick = onStartPomodoro,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "شروع پومودورو",
                                tint = PomodoroWork,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onComplete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "انجام شد",
                            tint = AccentGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = AccentRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف کار") },
            text = { Text("مطمئنی می‌خوای این کار رو حذف کنی؟") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("حذف", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun PriorityIndicator(priority: Priority) {
    val color = when (priority) {
        Priority.HIGH -> AccentRed
        Priority.MEDIUM -> AccentYellow
        Priority.LOW -> AccentGreen
    }
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

@Composable
private fun CategoryChip(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDarkCard)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun DueDateChip(date: LocalDate, isOverdue: Boolean) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isOverdue) AccentRed.copy(alpha = 0.2f) else SurfaceDarkCard)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                tint = if (isOverdue) AccentRed else TextSecondary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = date.format(formatter),
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverdue) AccentRed else TextSecondary
            )
        }
    }
}

@Composable
private fun PomodoroChip(completed: Int, estimated: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PomodoroWork.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🍅", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$completed/$estimated",
                style = MaterialTheme.typography.bodySmall,
                color = PomodoroWork
            )
        }
    }
}
