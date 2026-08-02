package com.focusflow.app.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.focusflow.app.FocusFlowApp
import com.focusflow.app.MainActivity
import com.focusflow.app.R
import com.focusflow.app.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class DueDateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val dueToday = taskRepository.getTasksDueOn(today)
        val overdue = taskRepository.getOverdueOrDueBy(today.minusDays(1))
            .filter { it.dueDate != null && it.dueDate.isBefore(today) }

        when {
            overdue.isNotEmpty() -> {
                val titles = overdue.take(3).joinToString("، ") { it.title }
                showNotification(
                    title = "کارهای عقب‌افتاده! ⚠️",
                    message = if (overdue.size == 1) "«${overdue.first().title}» از تاریخ گذشته"
                    else "${overdue.size} کار عقب افتاده: $titles",
                    notifId = NOTIF_OVERDUE
                )
            }
            dueToday.isNotEmpty() -> {
                val titles = dueToday.take(3).joinToString("، ") { it.title }
                showNotification(
                    title = "سررسید امروز 📅",
                    message = if (dueToday.size == 1) "امروز باید «${dueToday.first().title}» رو تموم کنی"
                    else "${dueToday.size} کار برای امروز: $titles",
                    notifId = NOTIF_DUE_TODAY
                )
            }
        }

        // Evening reminder for tomorrow's due tasks
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour >= 19) {
            val tomorrow = taskRepository.getTasksDueOn(today.plusDays(1))
            if (tomorrow.isNotEmpty()) {
                showNotification(
                    title = "فردا سررسید داری 🔔",
                    message = if (tomorrow.size == 1) "فردا «${tomorrow.first().title}» تموم می‌شه"
                    else "${tomorrow.size} کار فردا سررسید داره — امشب برنامه‌ریزی کن",
                    notifId = NOTIF_DUE_TOMORROW
                )
            }
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String, notifId: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            applicationContext, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(applicationContext, FocusFlowApp.CHANNEL_DUE)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager.notify(notifId, notification)
    }

    companion object {
        const val WORK_NAME = "due_date_reminder"
        const val NOTIF_DUE_TODAY = 2001
        const val NOTIF_OVERDUE = 2002
        const val NOTIF_DUE_TOMORROW = 2003

        fun schedule(context: Context) {
            // Morning check ~9:00
            val morning = PeriodicWorkRequestBuilder<DueDateWorker>(12, TimeUnit.HOURS)
                .setInitialDelay(calculateDelayToHour(9), TimeUnit.MILLISECONDS)
                .addTag("due_morning")
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                morning
            )
        }

        private fun calculateDelayToHour(hour: Int): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DATE, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}
