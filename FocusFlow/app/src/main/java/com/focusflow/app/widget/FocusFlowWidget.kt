package com.focusflow.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.focusflow.app.MainActivity
import com.focusflow.app.R
import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.domain.model.UserStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class FocusFlowWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        FocusFlowWidgetUpdater.updateAll(context)
    }

    override fun onEnabled(context: Context) {
        FocusFlowWidgetUpdater.updateAll(context)
    }
}

object FocusFlowWidgetUpdater {
    fun updateAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FocusFlowDatabase_get(context)
                val activeCount = db.taskDao().getActiveCount()
                val today = LocalDate.now().toString()
                val commitment = db.commitmentDao().getCommitment(today)
                val commitmentIds = commitment?.taskIdList().orEmpty()
                var commitmentDone = 0
                commitmentIds.forEach { id ->
                    if (db.taskDao().getTaskById(id)?.isCompleted == true) commitmentDone++
                }
                val totalPoints = db.dailyStatsDao().getTotalPoints() ?: 0
                val (_, levelTitle) = UserStats.levelFromPoints(totalPoints)

                // Simple streak: count consecutive days with activity
                val allStats = db.dailyStatsDao().getAllStats()
                val activeDays = allStats
                    .filter { it.tasksCompleted > 0 || it.pomodorosCompleted > 0 }
                    .map { it.date }
                    .sortedDescending()
                var streak = 0
                val todayDate = LocalDate.now()
                if (activeDays.isNotEmpty() &&
                    (activeDays.first() == todayDate || activeDays.first() == todayDate.minusDays(1))
                ) {
                    streak = 1
                    for (i in 1 until activeDays.size) {
                        if (activeDays[i] == activeDays[i - 1].minusDays(1)) streak++
                        else break
                    }
                }

                val commitmentText = when {
                    commitmentIds.isEmpty() -> "تعهد تنظیم نشده"
                    commitmentDone >= commitmentIds.size -> "تعهد کامل ✓"
                    else -> "تعهد: $commitmentDone/${commitmentIds.size}"
                }

                val views = RemoteViews(context.packageName, R.layout.widget_focusflow).apply {
                    setTextViewText(R.id.widget_tasks, "$activeCount کار باقی‌مانده")
                    setTextViewText(R.id.widget_streak, if (streak > 0) "🔥 $streak روز" else "💤 بدون streak")
                    setTextViewText(R.id.widget_commitment, commitmentText)
                    setTextViewText(R.id.widget_level, levelTitle)

                    val openIntent = Intent(context, MainActivity::class.java)
                    val pending = PendingIntent.getActivity(
                        context, 0, openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_root, pending)
                }

                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, FocusFlowWidget::class.java))
                ids.forEach { manager.updateAppWidget(it, views) }
            } catch (_: Exception) {
                // DB might not be ready yet
            }
        }
    }

    /**
     * Lightweight DB access for widget (avoids Hilt in AppWidgetProvider).
     * Uses same DB file; Room singleton via Room.databaseBuilder is fine for widget process.
     */
    @Suppress("FunctionName")
    private fun FocusFlowDatabase_get(context: Context): FocusFlowDatabase {
        return androidx.room.Room.databaseBuilder(
            context.applicationContext,
            FocusFlowDatabase::class.java,
            "focusflow.db"
        ).fallbackToDestructiveMigration().build()
    }
}
