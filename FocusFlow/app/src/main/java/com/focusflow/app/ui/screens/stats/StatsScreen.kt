package com.focusflow.app.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.app.domain.model.DailyStats
import com.focusflow.app.domain.model.UserStats
import com.focusflow.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text(
                "پیشرفتم",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.padding(start = 16.dp, top = 32.dp, bottom = 16.dp)
            )
        }

        item { LevelCard(stats = uiState.userStats) }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item { StreakCard(streak = uiState.userStats.currentStreak, longest = uiState.userStats.longestStreak) }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    value = "${uiState.userStats.totalTasksCompleted}",
                    label = "کار\nانجام‌شده",
                    color = AccentGreen
                )
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                    value = "${uiState.userStats.totalPomodoros}",
                    label = "پومودورو\nانجام‌شده",
                    color = PomodoroWork
                )
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Star,
                    value = "${uiState.userStats.totalPoints}",
                    label = "امتیاز\nکل",
                    color = AccentYellow
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        if (uiState.last30Days.isNotEmpty()) {
            item { ActivityHeatmap(days = uiState.last30Days) }
        }
    }
}

@Composable
private fun LevelCard(stats: UserStats) {
    val nextLevelPoints = UserStats.pointsForNextLevel(stats.totalPoints)
    val progress = if (nextLevelPoints > 0) stats.totalPoints.toFloat() / nextLevelPoints.toFloat() else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("سطح ${stats.level}", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    Text(
                        stats.levelTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.2f))
                ) {
                    Text("${stats.level}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { minOf(progress, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryPurple,
                trackColor = SurfaceDarkCard
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${stats.totalPoints} امتیاز", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("هدف: $nextLevelPoints", style = MaterialTheme.typography.bodySmall, color = TextHint)
            }
        }
    }
}

@Composable
private fun StreakCard(streak: Int, longest: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (streak > 0) StreakFire.copy(alpha = 0.15f) else CardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (streak > 0) "🔥" else "💤",
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (streak > 0) "streak فعال!" else "streak نداری",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (streak > 0) StreakFire else TextSecondary
                )
                Text(
                    if (streak > 0) "$streak روز پشت سرهم فعال بودی" else "امروز یه کار انجام بده!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$longest", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StreakFireLight)
                Text("بیشترین", style = MaterialTheme.typography.bodySmall, color = TextHint)
            }
        }
    }
}

@Composable
private fun StatMiniCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ActivityHeatmap(days: List<DailyStats>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("فعالیت ۳۰ روز گذشته", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))

            val sortedDays = days.sortedBy { it.date }
            val maxTasks = sortedDays.maxOfOrNull { it.tasksCompleted } ?: 1

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sortedDays.takeLast(30).forEach { stat ->
                    val intensity = if (maxTasks > 0) stat.tasksCompleted.toFloat() / maxTasks else 0f
                    val color = when {
                        intensity == 0f -> DividerColor
                        intensity < 0.33f -> AccentGreen.copy(alpha = 0.3f)
                        intensity < 0.66f -> AccentGreen.copy(alpha = 0.6f)
                        else -> AccentGreen
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("۳۰ روز پیش", style = MaterialTheme.typography.bodySmall, color = TextHint)
                Text("امروز", style = MaterialTheme.typography.bodySmall, color = TextHint)
            }
        }
    }
}
