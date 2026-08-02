package com.focusflow.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.model.Task
import com.focusflow.app.ui.theme.*

@Composable
fun CommitmentPickerSheet(
    tasks: List<Task>,
    onDismiss: () -> Unit,
    onConfirm: (List<Long>) -> Unit
) {
    var selected by remember { mutableStateOf(setOf<Long>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            "تعهد امروز (۱ تا ۳ کار)",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "فقط کارهایی رو انتخاب کن که واقعاً امروز تموم می‌کنی",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (tasks.isEmpty()) {
            Text("اول چند تا کار اضافه کن", color = TextSecondary)
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    val isSelected = task.id in selected
                    val canSelect = isSelected || selected.size < 3
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = isSelected,
                                enabled = canSelect,
                                role = Role.Checkbox,
                                onValueChange = { checked ->
                                    selected = if (checked) selected + task.id else selected - task.id
                                }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = AccentOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(task.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${task.priority.emoji} ${task.category.label}",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (selected.isNotEmpty()) {
                    onConfirm(selected.toList())
                    onDismiss()
                }
            },
            enabled = selected.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("ثبت تعهد (${selected.size})")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("انصراف", color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
