package com.focusflow.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusflow.app.widget.FocusFlowWidgetUpdater

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderWorker.schedule(context)
            StreakWorker.schedule(context)
            DueDateWorker.schedule(context)
            FocusFlowWidgetUpdater.updateAll(context)
        }
    }
}
