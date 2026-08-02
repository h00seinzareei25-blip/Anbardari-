package com.focusflow.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.data.repository.StatsRepository
import com.focusflow.app.domain.model.DailyStats
import com.focusflow.app.domain.model.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val userStats: UserStats = UserStats(),
    val last30Days: List<DailyStats> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            statsRepository.getLast30DaysStats().collect { days ->
                _uiState.update { it.copy(last30Days = days, isLoading = false) }
            }
        }
        loadUserStats()
    }

    private fun loadUserStats() {
        viewModelScope.launch {
            val stats = statsRepository.getUserStats()
            _uiState.update { it.copy(userStats = stats) }
        }
    }

    fun refresh() = loadUserStats()
}
