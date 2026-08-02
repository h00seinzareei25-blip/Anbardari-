package com.focusflow.app.data.repository

import com.focusflow.app.data.db.CommitmentDao
import com.focusflow.app.data.db.DailyStatsDao
import com.focusflow.app.data.db.TaskDao
import com.focusflow.app.domain.model.DailyCommitment
import com.focusflow.app.domain.model.DailyStats
import com.focusflow.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class CommitmentProgress(
    val commitment: DailyCommitment?,
    val committedTasks: List<Task> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val isFullyDone: Boolean = false
)

@Singleton
class CommitmentRepository @Inject constructor(
    private val commitmentDao: CommitmentDao,
    private val taskDao: TaskDao,
    private val dailyStatsDao: DailyStatsDao
) {
    fun getTodayProgress(): Flow<CommitmentProgress> {
        val today = LocalDate.now().toString()
        return combine(
            commitmentDao.getCommitmentForDate(today),
            taskDao.getActiveTasks(),
            taskDao.getCompletedTasks()
        ) { commitment, active, completed ->
            if (commitment == null) {
                CommitmentProgress(commitment = null)
            } else {
                val ids = commitment.taskIdList().toSet()
                val allRelevant = (active + completed).filter { it.id in ids }
                val done = allRelevant.count { it.isCompleted }
                val total = ids.size
                CommitmentProgress(
                    commitment = commitment,
                    committedTasks = allRelevant.sortedBy { it.isCompleted },
                    completedCount = done,
                    totalCount = total,
                    isFullyDone = total > 0 && done >= total
                )
            }
        }
    }

    suspend fun setTodayCommitment(taskIds: List<Long>) {
        require(taskIds.isNotEmpty())
        require(taskIds.size <= 3)
        commitmentDao.upsert(DailyCommitment.fromTaskIds(LocalDate.now(), taskIds.distinct()))
    }

    suspend fun clearToday() {
        commitmentDao.clear(LocalDate.now().toString())
    }

    /** Awards +50 points once when all committed tasks are done. */
    suspend fun tryAwardCompletionBonus(): Boolean {
        val today = LocalDate.now().toString()
        val commitment = commitmentDao.getCommitment(today) ?: return false
        if (commitment.bonusAwarded) return false

        val ids = commitment.taskIdList()
        if (ids.isEmpty()) return false

        val allDone = ids.all { id ->
            taskDao.getTaskById(id)?.isCompleted == true
        }
        if (!allDone) return false

        commitmentDao.upsert(commitment.copy(bonusAwarded = true))
        val existing = dailyStatsDao.getStatsForDate(today)
        val updated = existing?.copy(pointsEarned = existing.pointsEarned + 50)
            ?: DailyStats(date = LocalDate.now(), pointsEarned = 50)
        dailyStatsDao.insertOrUpdate(updated)
        return true
    }
}
