package com.focusflow.app.ui.screens.pomodoro

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.data.repository.TaskRepository
import com.focusflow.app.domain.model.PomodoroState
import com.focusflow.app.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PomodoroUiState(
    val state: PomodoroState = PomodoroState.IDLE,
    val timeRemainingMs: Long = 25 * 60 * 1000L,
    val totalDurationMs: Long = 25 * 60 * 1000L,
    val sessionCount: Int = 0,
    val selectedTask: Task? = null,
    val activeTasks: List<Task> = emptyList(),
    val motivationalMessage: String = "آماده‌ای؟ بزن بریم! 🚀"
)

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timer: CountDownTimer? = null

    init {
        viewModelScope.launch {
            taskRepository.getActiveTasks().collect { tasks ->
                _uiState.update { it.copy(activeTasks = tasks) }
            }
        }
    }

    fun selectTask(task: Task?) {
        _uiState.update { it.copy(selectedTask = task) }
    }

    fun startWork() {
        stopTimer()
        val duration = WORK_DURATION_MS
        _uiState.update {
            it.copy(
                state = PomodoroState.WORK,
                timeRemainingMs = duration,
                totalDurationMs = duration,
                motivationalMessage = getWorkMessage()
            )
        }
        startTimer(duration) { onWorkFinished() }
    }

    fun pause() {
        timer?.cancel()
        _uiState.update { it.copy(state = PomodoroState.PAUSED) }
    }

    fun resume() {
        val remaining = _uiState.value.timeRemainingMs
        val wasWorking = _uiState.value.sessionCount >= 0
        _uiState.update { it.copy(state = PomodoroState.WORK) }
        startTimer(remaining) { onWorkFinished() }
    }

    fun skipBreak() {
        stopTimer()
        startWork()
    }

    fun reset() {
        stopTimer()
        _uiState.update {
            PomodoroUiState(
                activeTasks = it.activeTasks,
                selectedTask = it.selectedTask,
                motivationalMessage = "آماده‌ای؟ بزن بریم! 🚀"
            )
        }
    }

    private fun onWorkFinished() {
        val newCount = _uiState.value.sessionCount + 1
        viewModelScope.launch {
            val selectedTask = _uiState.value.selectedTask
            if (selectedTask != null) {
                taskRepository.addPomodoroToTask(selectedTask.id, 25)
            } else {
                taskRepository.addPomodoroWithoutTask(25)
            }
        }

        val isLongBreak = newCount % 4 == 0
        val breakDuration = if (isLongBreak) LONG_BREAK_MS else SHORT_BREAK_MS
        val breakState = if (isLongBreak) PomodoroState.LONG_BREAK else PomodoroState.SHORT_BREAK

        _uiState.update {
            it.copy(
                state = breakState,
                sessionCount = newCount,
                timeRemainingMs = breakDuration,
                totalDurationMs = breakDuration,
                motivationalMessage = if (isLongBreak) "عالی! ۴ سشن تموم کردی. استراحت طولانی بکن! ☕" else "آفرین! ۵ دقیقه استراحت کن 😊"
            )
        }
        startTimer(breakDuration) { onBreakFinished() }
    }

    private fun onBreakFinished() {
        _uiState.update {
            it.copy(
                state = PomodoroState.IDLE,
                motivationalMessage = "استراحت تموم شد! آماده‌ای برای رند بعدی? 💪"
            )
        }
    }

    private fun startTimer(durationMs: Long, onFinish: () -> Unit) {
        timer = object : CountDownTimer(durationMs, 100L) {
            override fun onTick(remaining: Long) {
                _uiState.update { it.copy(timeRemainingMs = remaining) }
            }
            override fun onFinish() {
                onFinish()
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun getWorkMessage(): String {
        val messages = listOf(
            "تمرکز کن! الان وقت کاره 🎯",
            "ادامه بده، به نتیجه می‌رسی! 💪",
            "قدم به قدم، کوه رو هم میشه رفت 🏔️",
            "تنبلی رو بذار کنار! وقت عمله 🚀",
            "این ۲۵ دقیقه متعلق به توئه 🔥"
        )
        return messages.random()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    companion object {
        const val WORK_DURATION_MS = 25 * 60 * 1000L
        const val SHORT_BREAK_MS = 5 * 60 * 1000L
        const val LONG_BREAK_MS = 15 * 60 * 1000L
    }
}
