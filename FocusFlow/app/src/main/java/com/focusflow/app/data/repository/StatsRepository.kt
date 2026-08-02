package com.focusflow.app.data.repository

import com.focusflow.app.data.db.DailyStatsDao
import com.focusflow.app.data.db.PomodoroDao
import com.focusflow.app.data.db.TaskDao
import com.focusflow.app.domain.model.DailyStats
import com.focusflow.app.domain.model.UserStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val dailyStatsDao: DailyStatsDao,
    private val taskDao: TaskDao,
    private val pomodoroDao: PomodoroDao
) {
    fun getLast30DaysStats(): Flow<List<DailyStats>> = dailyStatsDao.getLast30Days()

    suspend fun getUserStats(): UserStats {
        val totalPoints = dailyStatsDao.getTotalPoints() ?: 0
        val totalPomodoros = pomodoroDao.getTotalPomodoros()
        val totalCompleted = taskDao.getTotalCompleted()
        val streak = calculateStreak()
        val (level, title) = UserStats.levelFromPoints(totalPoints)
        return UserStats(
            totalPoints = totalPoints,
            currentStreak = streak.first,
            longestStreak = streak.second,
            totalTasksCompleted = totalCompleted,
            totalPomodoros = totalPomodoros,
            level = level,
            levelTitle = title
        )
    }

    private suspend fun calculateStreak(): Pair<Int, Int> {
        val allStats = dailyStatsDao.getAllStats()
        if (allStats.isEmpty()) return Pair(0, 0)

        val activeDays = allStats
            .filter { it.tasksCompleted > 0 || it.pomodorosCompleted > 0 }
            .map { it.date }
            .sortedDescending()

        if (activeDays.isEmpty()) return Pair(0, 0)

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 1
        val today = LocalDate.now()

        if (activeDays.first() == today || activeDays.first() == today.minusDays(1)) {
            currentStreak = 1
            for (i in 1 until activeDays.size) {
                if (activeDays[i] == activeDays[i - 1].minusDays(1)) {
                    currentStreak++
                } else break
            }
        }

        for (i in 1 until activeDays.size) {
            if (activeDays[i] == activeDays[i - 1].minusDays(1)) {
                tempStreak++
                if (tempStreak > longestStreak) longestStreak = tempStreak
            } else {
                tempStreak = 1
            }
        }
        if (longestStreak == 0 && activeDays.isNotEmpty()) longestStreak = 1
        if (currentStreak > longestStreak) longestStreak = currentStreak

        return Pair(currentStreak, longestStreak)
    }
}
