package com.example.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.repository.AnalyticsTimeRange
import com.example.data.repository.CallAnalyticsRepository
import com.example.data.repository.CallAnalyticsSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Ready(val summary: CallAnalyticsSummary) : AnalyticsUiState()
    object Empty : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

class CallAnalyticsViewModel(
    private val repository: CallAnalyticsRepository = SalimApplication.instance.callAnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _selectedRange = MutableStateFlow(AnalyticsTimeRange.LAST_7_DAYS)
    val selectedRange: StateFlow<AnalyticsTimeRange> = _selectedRange.asStateFlow()

    init {
        loadAnalytics(_selectedRange.value)
    }

    fun setTimeRange(range: AnalyticsTimeRange) {
        _selectedRange.value = range
        loadAnalytics(range)
    }

    fun loadAnalytics(range: AnalyticsTimeRange = _selectedRange.value) {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading
            try {
                val summary = repository.getAnalytics(range)
                if (summary.totalCalls == 0) {
                    _uiState.value = AnalyticsUiState.Empty
                } else {
                    _uiState.value = AnalyticsUiState.Ready(summary)
                }
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(e.message ?: "Failed to calculate analytics")
            }
        }
    }
}
