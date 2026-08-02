package com.focusflow.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

enum class Priority(val label: String, val emoji: String) {
    LOW("کم", "🟢"),
    MEDIUM("متوسط", "🟡"),
    HIGH("زیاد", "🔴")
}

enum class TaskCategory(val label: String, val emoji: String) {
    WORK("کار", "💼"),
    PERSONAL("شخصی", "👤"),
    HEALTH("سلامت", "🏃"),
    LEARNING("یادگیری", "📚"),
    OTHER("سایر", "📌")
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val category: TaskCategory = TaskCategory.OTHER,
    val dueDate: LocalDate? = null,
    val estimatedPomodoros: Int = 1,
    val completedPomodoros: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val points: Int = 0
)
