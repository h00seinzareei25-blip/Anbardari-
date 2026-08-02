package com.focusflow.app.ui.screens.pomodoro

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.app.domain.model.PomodoroState
import com.focusflow.app.domain.model.Task
import com.focusflow.app.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(viewModel: PomodoroViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "تایمر پومودورو",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        StateLabel(state = uiState.state)

        Spacer(modifier = Modifier.height(16.dp))

        CircularTimer(
            timeRemainingMs = uiState.timeRemainingMs,
            totalDurationMs = uiState.totalDurationMs,
            state = uiState.state
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            uiState.motivationalMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        SessionDots(sessionCount = uiState.sessionCount)

        Spacer(modifier = Modifier.height(24.dp))

        ControlButtons(
            state = uiState.state,
            onStart = viewModel::startWork,
            onPause = viewModel::pause,
            onResume = viewModel::resume,
            onSkip = viewModel::skipBreak,
            onReset = viewModel::reset
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.activeTasks.isNotEmpty()) {
            Text("انتخاب کار", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    TaskChip(
                        label = "بدون کار",
                        isSelected = uiState.selectedTask == null,
                        onClick = { viewModel.selectTask(null) }
                    )
                }
                items(uiState.activeTasks.take(5)) { task ->
                    TaskChip(
                        label = task.title,
                        isSelected = uiState.selectedTask?.id == task.id,
                        onClick = { viewModel.selectTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StateLabel(state: PomodoroState) {
    val (label, color) = when (state) {
        PomodoroState.IDLE -> Pair("آماده", PrimaryPurple)
        PomodoroState.WORK -> Pair("🔴 زمان کار", PomodoroWork)
        PomodoroState.SHORT_BREAK -> Pair("🟢 استراحت کوتاه", PomodoroBreak)
        PomodoroState.LONG_BREAK -> Pair("🟢 استراحت طولانی", PomodoroBreak)
        PomodoroState.PAUSED -> Pair("⏸ مکث", PomodoroPause)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(label, color = color, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CircularTimer(
    timeRemainingMs: Long,
    totalDurationMs: Long,
    state: PomodoroState
) {
    val progress = if (totalDurationMs > 0) timeRemainingMs.toFloat() / totalDurationMs.toFloat() else 1f
    val minutes = (timeRemainingMs / 1000 / 60).toInt()
    val seconds = (timeRemainingMs / 1000 % 60).toInt()

    val timerColor = when (state) {
        PomodoroState.WORK -> PomodoroWork
        PomodoroState.SHORT_BREAK, PomodoroState.LONG_BREAK -> PomodoroBreak
        PomodoroState.PAUSED -> PomodoroPause
        PomodoroState.IDLE -> PrimaryPurple
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "timer_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == PomodoroState.WORK) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 16.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            drawCircle(
                color = timerColor.copy(alpha = 0.15f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = timerColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun SessionDots(sessionCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(4) { index ->
            val completed = index < (sessionCount % 4)
            val isLongBreak = sessionCount > 0 && sessionCount % 4 == 0 && index == 3
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isLongBreak -> PomodoroBreak
                            completed -> PomodoroWork
                            else -> DividerColor
                        }
                    )
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "سشن ${sessionCount + 1}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun ControlButtons(
    state: PomodoroState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state != PomodoroState.IDLE) {
            OutlinedButton(
                onClick = onReset,
                shape = CircleShape,
                modifier = Modifier.size(52.dp),
                contentPadding = PaddingValues(0.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(listOf(DividerColor, DividerColor))
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "ریست", tint = TextSecondary)
            }
        }

        Button(
            onClick = when (state) {
                PomodoroState.IDLE -> onStart
                PomodoroState.WORK -> onPause
                PomodoroState.PAUSED -> onResume
                PomodoroState.SHORT_BREAK, PomodoroState.LONG_BREAK -> onSkip
            },
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (state) {
                    PomodoroState.WORK -> PomodoroWork
                    PomodoroState.SHORT_BREAK, PomodoroState.LONG_BREAK -> PomodoroBreak
                    else -> PrimaryPurple
                }
            )
        ) {
            Icon(
                imageVector = when (state) {
                    PomodoroState.IDLE -> Icons.Default.PlayArrow
                    PomodoroState.WORK -> Icons.Default.Pause
                    PomodoroState.PAUSED -> Icons.Default.PlayArrow
                    PomodoroState.SHORT_BREAK, PomodoroState.LONG_BREAK -> Icons.Default.SkipNext
                },
                contentDescription = "کنترل تایمر",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun TaskChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                label,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryPurple,
            selectedLabelColor = Color.White,
            containerColor = CardBackground,
            labelColor = TextSecondary
        )
    )
}
