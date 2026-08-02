package com.focusflow.app.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.focusflow.app.FocusFlowApp
import com.focusflow.app.R
import com.focusflow.app.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val activeTasks = taskRepository.getActiveTasks().first()
        if (activeTasks.isEmpty()) return Result.success()

        val message = when {
            activeTasks.size == 1 -> "یه کار داری که منتظرته: ${activeTasks.first().title}"
            else -> "${activeTasks.size} کار داری که باید انجام بدی. شروع کن! 💪"
        }

        showNotification(
            title = "وقت کار کردنه! 🎯",
            message = message,
            notifId = NOTIF_ID_REMINDER
        )
        return Result.success()
    }

    private fun showNotification(title: String, message: String, notifId: Int) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(applicationContext, FocusFlowApp.CHANNEL_REMINDER)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(notifId, notification)
    }

    companion object {
        const val WORK_NAME = "daily_reminder"
        const val NOTIF_ID_REMINDER = 1001

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(4, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
