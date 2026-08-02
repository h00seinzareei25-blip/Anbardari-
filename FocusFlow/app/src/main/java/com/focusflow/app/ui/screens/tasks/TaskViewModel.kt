package com.focusflow.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.data.repository.TaskRepository
import com.focusflow.app.domain.model.Task
import com.focusflow.app.domain.model.TaskCategory
import com.focusflow.app.domain.model.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TaskUiState(
    val activeTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val showCompleted: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _showCompleted = MutableStateFlow(false)
    val uiState: StateFlow<TaskUiState> = combine(
        taskRepository.getActiveTasks(),
        taskRepository.getCompletedTasks(),
        _showCompleted
    ) { active, completed, showCompleted ->
        TaskUiState(
            activeTasks = active,
            completedTasks = completed,
            showCompleted = showCompleted
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
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch { taskRepository.completeTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { taskRepository.deleteTask(task) }
    }

    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
    }
}
