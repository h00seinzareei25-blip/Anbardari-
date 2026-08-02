package com.focusflow.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import com.focusflow.app.ui.screens.MainNavGraph
import com.focusflow.app.ui.theme.FocusFlowTheme
import com.focusflow.app.widget.FocusFlowWidgetUpdater
import com.focusflow.app.worker.DueDateWorker
import com.focusflow.app.worker.ReminderWorker
import com.focusflow.app.worker.StreakWorker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ReminderWorker.schedule(this)
        StreakWorker.schedule(this)
        DueDateWorker.schedule(this)
        FocusFlowWidgetUpdater.updateAll(this)

        val startTaskId = intent?.getLongExtra(EXTRA_TASK_ID, -1L)?.takeIf { it > 0 }

        setContent {
            FocusFlowTheme {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notifPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
                    SideEffect {
                        if (!notifPermission.status.isGranted) {
                            notifPermission.launchPermissionRequest()
                        }
                    }
                }
                MainNavGraph(initialTaskId = startTaskId)
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}
