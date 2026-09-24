package com.example.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.model.ContactItem
import com.example.data.repository.BlockedRepository
import com.example.data.repository.CallLogRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.TelecomRepository
import com.example.util.PermissionHelper
import com.example.util.RoleHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeSummaryState(
    val isDefaultDialer: Boolean = false,
    val hasCorePermissions: Boolean = false,
    val favorites: List<ContactItem> = emptyList(),
    val recentCalls: List<CallRecord> = emptyList(),
    val missedCallsCount: Int = 0,
    val blockedNumbersCount: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val contactsRepository: ContactsRepository = SalimApplication.instance.contactsRepository,
    private val callLogRepository: CallLogRepository = SalimApplication.instance.callLogRepository,
    private val blockedRepository: BlockedRepository = SalimApplication.instance.blockedRepository,
    private val telecomRepository: TelecomRepository = SalimApplication.instance.telecomRepository,
    private val context: Context = SalimApplication.instance.applicationContext
) : ViewModel() {

    private val _state = MutableStateFlow(HomeSummaryState())
    val state: StateFlow<HomeSummaryState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val isDefault = RoleHelper.isDefaultDialer(context)
            val hasPerms = PermissionHelper.hasContactsPermission(context) && PermissionHelper.hasCallLogPermission(context)

            val allContacts = if (hasPerms) contactsRepository.loadContacts() else emptyList()
            val favorites = allContacts.filter { it.isFavorite }

            val allCalls = if (hasPerms) callLogRepository.loadCallLogs() else emptyList()
            val recentCalls = allCalls.take(5)
            val missedCount = allCalls.count { it.type == CallType.MISSED || it.type == CallType.REJECTED }

            _state.value = HomeSummaryState(
                isDefaultDialer = isDefault,
                hasCorePermissions = hasPerms,
                favorites = favorites,
                recentCalls = recentCalls,
                missedCallsCount = missedCount,
                blockedNumbersCount = 0, // updated via collection if available
                isLoading = false
            )
        }

        viewModelScope.launch {
            blockedRepository.allBlocked.collect { blockedList ->
                _state.value = _state.value.copy(blockedNumbersCount = blockedList.size)
            }
        }
    }

    fun makeCall(number: String) {
        telecomRepository.makeCall(number)
    }
}
