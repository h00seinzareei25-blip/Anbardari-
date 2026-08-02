package com.focusflow.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_commitments")
data class DailyCommitment(
    @PrimaryKey val date: LocalDate = LocalDate.now(),
    /** Comma-separated task IDs committed for today */
    val taskIds: String = "",
    val bonusAwarded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun taskIdList(): List<Long> =
        if (taskIds.isBlank()) emptyList()
        else taskIds.split(",").mapNotNull { it.trim().toLongOrNull() }

    companion object {
        fun fromTaskIds(date: LocalDate, ids: List<Long>) =
            DailyCommitment(date = date, taskIds = ids.joinToString(","))
    }
}
