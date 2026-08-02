package com.focusflow.app.di

import android.content.Context
import androidx.room.Room
import com.focusflow.app.data.db.DailyStatsDao
import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.data.db.PomodoroDao
import com.focusflow.app.data.db.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FocusFlowDatabase =
        Room.databaseBuilder(context, FocusFlowDatabase::class.java, "focusflow.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideTaskDao(db: FocusFlowDatabase): TaskDao = db.taskDao()
    @Provides fun providePomodoroDao(db: FocusFlowDatabase): PomodoroDao = db.pomodoroDao()
    @Provides fun provideDailyStatsDao(db: FocusFlowDatabase): DailyStatsDao = db.dailyStatsDao()
}
