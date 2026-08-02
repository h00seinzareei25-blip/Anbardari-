package com.focusflow.app.data.db

import androidx.room.*
import com.focusflow.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END, dueDate ASC NULLS LAST")
    fun getActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC LIMIT 50")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND completedAt >= :startOfDay AND completedAt < :endOfDay")
    suspend fun getCompletedCountForDate(startOfDay: String, endOfDay: String): Int

    @Query("SELECT SUM(points) FROM tasks WHERE isCompleted = 1")
    suspend fun getTotalPoints(): Int?

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    suspend fun getTotalCompleted(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET completedPomodoros = completedPomodoros + 1 WHERE id = :taskId")
    suspend fun incrementPomodoros(taskId: Long)
}
