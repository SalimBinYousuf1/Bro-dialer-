package com.example.ui.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.model.BlockedNumber
import com.example.data.repository.BlockedRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BlockedNumbersViewModel(
    private val blockedRepository: BlockedRepository = SalimApplication.instance.blockedRepository
) : ViewModel() {

    val blockedNumbers: StateFlow<List<BlockedNumber>> = blockedRepository
        .allBlocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun blockNumber(number: String, contactName: String? = null) {
        viewModelScope.launch {
            if (number.isNotBlank()) {
                blockedRepository.blockNumber(number, contactName)
            }
        }
    }

    fun unblock(blockedNumber: BlockedNumber) {
        viewModelScope.launch {
            blockedRepository.unblock(blockedNumber)
        }
    }
}
