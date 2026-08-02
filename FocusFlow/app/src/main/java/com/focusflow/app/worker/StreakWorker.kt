package com.focusflow.app.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.focusflow.app.FocusFlowApp
import com.focusflow.app.R
import com.focusflow.app.data.repository.StatsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class StreakWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val statsRepository: StatsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val stats = statsRepository.getUserStats()
        if (stats.currentStreak > 0) {
            showNotification(
                title = "streak تو در خطره! 🔥",
                message = "${stats.currentStreak} روز پشت سر هم کار کردی. امروز هم یه کار انجام بده!",
                notifId = NOTIF_ID_STREAK
            )
        }
        return Result.success()
    }

    private fun showNotification(title: String, message: String, notifId: Int) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(applicationContext, FocusFlowApp.CHANNEL_STREAK)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager.notify(notifId, notification)
    }

    companion object {
        const val WORK_NAME = "streak_reminder"
        const val NOTIF_ID_STREAK = 1002

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<StreakWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        private fun calculateInitialDelay(): Long {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 20)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                if (before(now)) add(java.util.Calendar.DATE, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}
