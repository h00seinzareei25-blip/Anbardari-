package com.focusflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusflow.app.data.repository.CommitmentProgress
import com.focusflow.app.ui.theme.*

@Composable
fun CommitmentCard(
    progress: CommitmentProgress,
    onSetCommitment: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (progress.isFullyDone) AccentGreen.copy(alpha = 0.15f)
            else if (progress.commitment != null) AccentOrange.copy(alpha = 0.12f)
            else CardBackground
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = if (progress.isFullyDone) AccentGreen else AccentOrange,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "تعهد امروز",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (progress.commitment != null && !progress.isFullyDone) {
                    IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "تغییر تعهد", tint = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                progress.commitment == null -> {
                    Text(
                        "صبح تصمیم بگیر امروز کدوم کارها رو تموم می‌کنی (حداکثر ۳ تا)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSetCommitment,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تعهد امروز رو مشخص کن")
                    }
                }
                progress.isFullyDone -> {
                    Text(
                        "عالی! همه تعهدات امروز تموم شد 🎉 (+۵۰ امتیاز)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AccentGreen
                    )
                }
                else -> {
                    Text(
                        "${progress.completedCount} از ${progress.totalCount} کار تعهدی انجام شد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val ratio = if (progress.totalCount > 0)
                        progress.completedCount.toFloat() / progress.totalCount else 0f
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentOrange,
                        trackColor = SurfaceDarkCard
                    )
                    if (progress.committedTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        progress.committedTasks.forEach { task ->
                            Text(
                                text = if (task.isCompleted) "✓ ${task.title}" else "○ ${task.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (task.isCompleted) AccentGreen else TextPrimary,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
