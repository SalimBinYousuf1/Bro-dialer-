package com.example.ui.recents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.repository.CallLogRepository
import com.example.data.repository.TelecomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RecentsFilter {
    ALL,
    MISSED
}

class RecentsViewModel(
    private val callLogRepository: CallLogRepository = SalimApplication.instance.callLogRepository,
    private val telecomRepository: TelecomRepository = SalimApplication.instance.telecomRepository
) : ViewModel() {

    private val _rawCalls = MutableStateFlow<List<CallRecord>>(emptyList())
    val rawCalls: StateFlow<List<CallRecord>> = _rawCalls.asStateFlow()

    private val _filter = MutableStateFlow(RecentsFilter.ALL)
    val filter: StateFlow<RecentsFilter> = _filter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    val filteredCalls: StateFlow<List<CallRecord>> = combine(
        _rawCalls,
        _filter
    ) { calls, filterMode ->
        when (filterMode) {
            RecentsFilter.ALL -> calls
            RecentsFilter.MISSED -> calls.filter { it.type == CallType.MISSED || it.type == CallType.REJECTED }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCallLogs()
    }

    fun loadCallLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            val logs = callLogRepository.loadCallLogs()
            _rawCalls.value = logs
            _isLoading.value = false
        }
    }

    fun setFilter(filter: RecentsFilter) {
        _filter.value = filter
    }

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedIds.value = emptySet()
        }
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
        if (current.isEmpty() && _isSelectionMode.value) {
            // Keep selection mode active until user cancels
        }
    }

    fun selectAll() {
        _selectedIds.value = filteredCalls.value.map { it.id }.toSet()
    }

    fun deselectAll() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val toDelete = _selectedIds.value
            if (toDelete.isNotEmpty()) {
                callLogRepository.deleteCallLogs(toDelete)
                _rawCalls.value = _rawCalls.value.filterNot { toDelete.contains(it.id) }
                _selectedIds.value = emptySet()
                _isSelectionMode.value = false
                onDone()
            }
        }
    }

    fun deleteCall(id: Long) {
        viewModelScope.launch {
            callLogRepository.deleteCallLog(id)
            _rawCalls.value = _rawCalls.value.filterNot { it.id == id }
        }
    }

    fun clearAllCallHistory() {
        viewModelScope.launch {
            callLogRepository.clearAllCallLogs()
            _rawCalls.value = emptyList()
            _selectedIds.value = emptySet()
            _isSelectionMode.value = false
        }
    }

    fun makeCall(number: String) {
        telecomRepository.makeCall(number)
    }

    fun sendSms(number: String) {
        telecomRepository.openSms(number)
    }

    fun blockNumber(number: String, contactName: String?, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            SalimApplication.instance.blockedRepository.blockNumber(number, contactName)
            onDone()
        }
    }
}
