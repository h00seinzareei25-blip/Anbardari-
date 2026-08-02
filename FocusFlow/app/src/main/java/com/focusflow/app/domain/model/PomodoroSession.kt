package com.focusflow.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

enum class PomodoroState {
    IDLE, WORK, SHORT_BREAK, LONG_BREAK, PAUSED
}

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String = "",
    val date: LocalDate = LocalDate.now(),
    val startedAt: LocalDateTime = LocalDateTime.now(),
    val durationMinutes: Int = 25,
    val isCompleted: Boolean = false
)
