package com.focusflow.app.data.db

import androidx.room.*
import com.focusflow.app.domain.model.DailyCommitment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommitmentDao {
    @Query("SELECT * FROM daily_commitments WHERE date = :date")
    fun getCommitmentForDate(date: String): Flow<DailyCommitment?>

    @Query("SELECT * FROM daily_commitments WHERE date = :date")
    suspend fun getCommitment(date: String): DailyCommitment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(commitment: DailyCommitment)

    @Query("DELETE FROM daily_commitments WHERE date = :date")
    suspend fun clear(date: String)
}
