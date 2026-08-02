package com.focusflow.app.data.db

import androidx.room.*
import com.focusflow.app.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 30")
    fun getLast30Days(): Flow<List<DailyStats>>

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getStatsForDate(date: String): DailyStats?

    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    suspend fun getAllStats(): List<DailyStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: DailyStats)

    @Query("SELECT SUM(pointsEarned) FROM daily_stats")
    suspend fun getTotalPoints(): Int?

    @Query("SELECT SUM(focusMinutes) FROM daily_stats")
    suspend fun getTotalFocusMinutes(): Int?
}
