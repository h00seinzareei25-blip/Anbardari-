package com.focusflow.app.data.repository

import com.focusflow.app.data.db.DailyStatsDao
import com.focusflow.app.data.db.TaskDao
import com.focusflow.app.domain.model.DailyStats
import com.focusflow.app.domain.model.Priority
import com.focusflow.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val dailyStatsDao: DailyStatsDao
) {
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()
    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()

    suspend fun addTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun completeTask(task: Task) {
        val points = calculatePoints(task)
        val completedTask = task.copy(
            isCompleted = true,
            completedAt = LocalDateTime.now(),
            points = points
        )
        taskDao.updateTask(completedTask)
        updateDailyStats(points = points, tasksCompleted = 1)
    }

    suspend fun getTotalPoints(): Int = taskDao.getTotalPoints() ?: 0

    suspend fun getTotalCompleted(): Int = taskDao.getTotalCompleted()

    suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)

    suspend fun getActiveCount(): Int = taskDao.getActiveCount()

    suspend fun getTasksDueOn(date: LocalDate): List<Task> =
        taskDao.getTasksDueOn(date.toString())

    suspend fun getOverdueOrDueBy(date: LocalDate): List<Task> =
        taskDao.getOverdueOrDueBy(date.toString())

    private fun calculatePoints(task: Task): Int {
        val basePoints = when (task.priority) {
            Priority.HIGH -> 30
            Priority.MEDIUM -> 20
            Priority.LOW -> 10
        }
        val dueDateBonus = if (task.dueDate != null && !task.dueDate.isBefore(LocalDate.now())) 10 else 0
        val pomodoroBonus = task.completedPomodoros * 5
        return basePoints + dueDateBonus + pomodoroBonus
    }

    private suspend fun updateDailyStats(points: Int = 0, tasksCompleted: Int = 0, pomodoros: Int = 0, focusMinutes: Int = 0) {
        val today = LocalDate.now().toString()
        val existing = dailyStatsDao.getStatsForDate(today)
        val updated = existing?.copy(
            tasksCompleted = existing.tasksCompleted + tasksCompleted,
            pomodorosCompleted = existing.pomodorosCompleted + pomodoros,
            pointsEarned = existing.pointsEarned + points,
            focusMinutes = existing.focusMinutes + focusMinutes
        ) ?: DailyStats(
            date = LocalDate.now(),
            tasksCompleted = tasksCompleted,
            pomodorosCompleted = pomodoros,
            pointsEarned = points,
            focusMinutes = focusMinutes
        )
        dailyStatsDao.insertOrUpdate(updated)
    }

    suspend fun addPomodoroToTask(taskId: Long, focusMinutes: Int) {
        taskDao.incrementPomodoros(taskId)
        updateDailyStats(pomodoros = 1, focusMinutes = focusMinutes, points = 15)
    }

    suspend fun addPomodoroWithoutTask(focusMinutes: Int) {
        updateDailyStats(pomodoros = 1, focusMinutes = focusMinutes, points = 15)
    }
}
