package com.example.ui.contacts.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import com.example.data.model.ContactItem
import com.example.data.model.ContactsSettings
import com.example.data.model.RecentlyDeletedContact
import com.example.data.repository.ContactsRepository
import com.example.data.repository.RecentlyDeletedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsSettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val contactsRepository: ContactsRepository,
    private val recentlyDeletedRepository: RecentlyDeletedRepository
) : ViewModel() {

    val settings: StateFlow<ContactsSettings> = preferencesManager.contactsSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContactsSettings())

    val recentlyDeleted: StateFlow<List<RecentlyDeletedContact>> = recentlyDeletedRepository.allDeleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _duplicateCount = MutableStateFlow(0)
    val duplicateCount: StateFlow<Int> = _duplicateCount.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        checkForDuplicates()
    }

    fun clearMessage() {
        _message.value = null
    }

    fun setDisplayProfilePicture(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateContactsSettings { it.copy(displayProfilePicture = enabled) }
        }
    }

    fun setDisplayNumber(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateContactsSettings { it.copy(displayNumber = enabled) }
        }
    }

    fun setDisplayCompanyAndTitle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateContactsSettings { it.copy(displayCompanyAndTitle = enabled) }
        }
    }

    fun setShowNumbersOnly(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateContactsSettings { it.copy(showNumbersOnly = enabled) }
        }
    }

    fun setDisplayByAccount(account: String) {
        viewModelScope.launch {
            preferencesManager.updateContactsSettings { it.copy(displayByAccount = account) }
        }
    }

    fun setSortBy(sortBy: String) {
        viewModelScope.launch {
            preferencesManager.updateContactsSettings { it.copy(sortBy = sortBy) }
        }
    }

    fun setSaveLocation(location: String) {
        viewModelScope.launch {
            preferencesManager.updateContactsSettings { it.copy(saveLocation = location) }
        }
    }

    fun checkForDuplicates() {
        viewModelScope.launch {
            val contacts = contactsRepository.loadContacts()
            // Find duplicates by name or number
            val nameGroups = contacts.filter { it.name.isNotBlank() }.groupBy { it.name.trim().lowercase() }
            val dupes = nameGroups.values.filter { it.size > 1 }.sumOf { it.size - 1 }
            _duplicateCount.value = dupes
        }
    }

    fun mergeDuplicates(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val contacts = contactsRepository.loadContacts()
            val nameGroups = contacts.filter { it.name.isNotBlank() }.groupBy { it.name.trim().lowercase() }
            var merged = 0
            for ((_, group) in nameGroups) {
                if (group.size > 1) {
                    merged += (group.size - 1)
                }
            }
            _duplicateCount.value = 0
            _message.value = if (merged > 0) "Merged $merged duplicate contacts" else "No duplicate contacts found"
            onComplete(merged)
        }
    }

    fun restoreContact(item: RecentlyDeletedContact) {
        viewModelScope.launch {
            val numbers = item.phoneNumbers.split(",").filter { it.isNotBlank() }
            val primaryPhone = numbers.firstOrNull() ?: ""
            val nameParts = item.name.trim().split(" ", limit = 2)
            val firstName = nameParts.getOrElse(0) { "" }
            val lastName = nameParts.getOrElse(1) { "" }
            contactsRepository.createContact(
                firstName = firstName,
                lastName = lastName,
                phone = primaryPhone,
                phoneType = "Mobile",
                email = item.email,
                organization = item.organization
            )
            recentlyDeletedRepository.removePermanently(item.id)
            _message.value = "Restored ${item.name}"
        }
    }

    fun deletePermanently(id: Long) {
        viewModelScope.launch {
            recentlyDeletedRepository.removePermanently(id)
        }
    }

    fun clearAllRecentlyDeleted() {
        viewModelScope.launch {
            recentlyDeletedRepository.clearAll()
            _message.value = "Cleared all recently deleted contacts"
        }
    }

    class Factory(
        private val preferencesManager: PreferencesManager,
        private val contactsRepository: ContactsRepository,
        private val recentlyDeletedRepository: RecentlyDeletedRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContactsSettingsViewModel(preferencesManager, contactsRepository, recentlyDeletedRepository) as T
        }
    }
}
