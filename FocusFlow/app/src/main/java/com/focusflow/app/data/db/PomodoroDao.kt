package com.focusflow.app.data.db

import androidx.room.*
import com.focusflow.app.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PomodoroDao {
    @Query("SELECT * FROM pomodoro_sessions WHERE date = :date AND isCompleted = 1")
    fun getSessionsForDate(date: String): Flow<List<PomodoroSession>>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE isCompleted = 1")
    suspend fun getTotalPomodoros(): Int

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE date = :date AND isCompleted = 1")
    suspend fun getPomodorosForDate(date: String): Int

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC LIMIT 30")
    fun getRecentSessions(): Flow<List<PomodoroSession>>

    @Insert
    suspend fun insertSession(session: PomodoroSession): Long

    @Update
    suspend fun updateSession(session: PomodoroSession)
}
