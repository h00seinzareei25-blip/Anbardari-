package com.focusflow.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FocusFlowApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val pomodoroChannel = NotificationChannel(
            CHANNEL_POMODORO,
            "تایمر پومودورو",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "اعلان‌های تایمر پومودورو"
        }

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER,
            "یادآوری کارها",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "یادآوری کارهای روزانه"
        }

        val streakChannel = NotificationChannel(
            CHANNEL_STREAK,
            "حفظ streak",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "اعلان برای حفظ streak روزانه"
        }

        val dueChannel = NotificationChannel(
            CHANNEL_DUE,
            "سررسید کارها",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "اعلان کارهای دارای سررسید و عقب‌افتاده"
        }

        manager.createNotificationChannels(
            listOf(pomodoroChannel, reminderChannel, streakChannel, dueChannel)
        )
    }

    companion object {
        const val CHANNEL_POMODORO = "channel_pomodoro"
        const val CHANNEL_REMINDER = "channel_reminder"
        const val CHANNEL_STREAK = "channel_streak"
        const val CHANNEL_DUE = "channel_due"
    }
}
