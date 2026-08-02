package com.focusflow.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_stats")
data class DailyStats(
    @PrimaryKey val date: LocalDate = LocalDate.now(),
    val tasksCompleted: Int = 0,
    val pomodorosCompleted: Int = 0,
    val pointsEarned: Int = 0,
    val focusMinutes: Int = 0
)

data class UserStats(
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalTasksCompleted: Int = 0,
    val totalPomodoros: Int = 0,
    val level: Int = 1,
    val levelTitle: String = "تازه‌کار"
) {
    companion object {
        fun levelFromPoints(points: Int): Pair<Int, String> {
            return when {
                points < 100 -> Pair(1, "تازه‌کار 🌱")
                points < 300 -> Pair(2, "در حال رشد 🌿")
                points < 600 -> Pair(3, "پیگیر 💪")
                points < 1000 -> Pair(4, "منضبط ⭐")
                points < 2000 -> Pair(5, "متمرکز 🎯")
                points < 3500 -> Pair(6, "قهرمان 🏆")
                points < 5000 -> Pair(7, "استاد 🧠")
                else -> Pair(8, "افسانه‌ای 🔥")
            }
        }

        fun pointsForNextLevel(currentPoints: Int): Int {
            return when {
                currentPoints < 100 -> 100
                currentPoints < 300 -> 300
                currentPoints < 600 -> 600
                currentPoints < 1000 -> 1000
                currentPoints < 2000 -> 2000
                currentPoints < 3500 -> 3500
                currentPoints < 5000 -> 5000
                else -> currentPoints + 1000
            }
        }
    }
}
