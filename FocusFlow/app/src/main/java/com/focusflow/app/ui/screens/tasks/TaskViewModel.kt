package com.focusflow.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.data.repository.CommitmentProgress
import com.focusflow.app.data.repository.CommitmentRepository
import com.focusflow.app.data.repository.TaskRepository
import com.focusflow.app.domain.model.Priority
import com.focusflow.app.domain.model.Task
import com.focusflow.app.domain.model.TaskCategory
import com.focusflow.app.widget.FocusFlowWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TaskUiState(
    val activeTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val showCompleted: Boolean = false,
    val commitment: CommitmentProgress = CommitmentProgress(null),
    val bonusJustAwarded: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val commitmentRepository: CommitmentRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _showCompleted = MutableStateFlow(false)
    private val _bonusJustAwarded = MutableStateFlow(false)

    val uiState: StateFlow<TaskUiState> = combine(
        taskRepository.getActiveTasks(),
        taskRepository.getCompletedTasks(),
        _showCompleted,
        commitmentRepository.getTodayProgress(),
        _bonusJustAwarded
    ) { active, completed, showCompleted, commitment, bonus ->
        TaskUiState(
            activeTasks = active,
            completedTasks = completed,
            showCompleted = showCompleted,
            commitment = commitment,
            bonusJustAwarded = bonus
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskUiState())

    fun addTask(
        title: String,
        description: String,
        priority: Priority,
        category: TaskCategory,
        dueDate: LocalDate?,
        estimatedPomodoros: Int
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepository.addTask(
                Task(
                    title = title.trim(),
                    description = description.trim(),
                    priority = priority,
                    category = category,
                    dueDate = dueDate,
                    estimatedPomodoros = estimatedPomodoros
                )
            )
            FocusFlowWidgetUpdater.updateAll(context)
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            taskRepository.completeTask(task)
            val awarded = commitmentRepository.tryAwardCompletionBonus()
            if (awarded) _bonusJustAwarded.value = true
            FocusFlowWidgetUpdater.updateAll(context)
        }
    }

    fun clearBonusFlag() {
        _bonusJustAwarded.value = false
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            FocusFlowWidgetUpdater.updateAll(context)
        }
    }

    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
    }

    fun setCommitment(taskIds: List<Long>) {
        viewModelScope.launch {
            commitmentRepository.setTodayCommitment(taskIds)
            FocusFlowWidgetUpdater.updateAll(context)
        }
    }

    fun clearCommitment() {
        viewModelScope.launch {
            commitmentRepository.clearToday()
            FocusFlowWidgetUpdater.updateAll(context)
        }
    }
}
