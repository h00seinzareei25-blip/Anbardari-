package com.focusflow.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.focusflow.app.domain.model.DailyStats
import com.focusflow.app.domain.model.PomodoroSession
import com.focusflow.app.domain.model.Task

@Database(
    entities = [Task::class, PomodoroSession::class, DailyStats::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun dailyStatsDao(): DailyStatsDao
}
