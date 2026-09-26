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

    private val _lastBackupTime = MutableStateFlow<String?>(null)
    val lastBackupTime: StateFlow<String?> = _lastBackupTime.asStateFlow()

    private val _hasBackup = MutableStateFlow(false)
    val hasBackup: StateFlow<Boolean> = _hasBackup.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        checkForDuplicates()
        refreshBackupStatus()
    }

    fun refreshBackupStatus() {
        val ctx = com.example.SalimApplication.instance.applicationContext
        _hasBackup.value = com.example.domain.usecase.VCardHelper.hasLocalBackup(ctx)
        _lastBackupTime.value = com.example.domain.usecase.VCardHelper.getLastBackupTime(ctx)
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
                    val duplicates = group.drop(1)
                    for (dup in duplicates) {
                        val deleted = contactsRepository.deleteContact(dup.id)
                        if (deleted) {
                            merged++
                        }
                    }
                }
            }
            checkForDuplicates()
            _message.value = if (merged > 0) "Merged and removed $merged duplicate contacts" else "No duplicate contacts to merge"
            onComplete(merged)
        }
    }

    fun backupContacts(context: Context) {
        viewModelScope.launch {
            val contacts = contactsRepository.loadContacts()
            if (contacts.isEmpty()) {
                _message.value = "No contacts found to back up"
                return@launch
            }
            val success = com.example.domain.usecase.VCardHelper.saveLocalBackup(context, contacts)
            if (success) {
                refreshBackupStatus()
                _message.value = "Backed up ${contacts.size} contacts successfully"
            } else {
                _message.value = "Failed to save contacts backup"
            }
        }
    }

    fun restoreLocalBackup(context: Context, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val parsed = com.example.domain.usecase.VCardHelper.readLocalBackup(context)
            if (parsed.isEmpty()) {
                _message.value = "No backup found or backup file is empty"
                return@launch
            }
            var restoredCount = 0
            for (item in parsed) {
                val ok = contactsRepository.createContact(
                    firstName = item.firstName,
                    lastName = item.lastName,
                    phone = item.phone,
                    phoneType = item.phoneType,
                    email = item.email,
                    organization = item.organization
                )
                if (ok) restoredCount++
            }
            checkForDuplicates()
            _message.value = "Restored $restoredCount contacts from backup"
            onComplete()
        }
    }

    fun exportContactsVcf(context: Context, onShareIntentReady: (android.content.Intent) -> Unit) {
        viewModelScope.launch {
            val contacts = contactsRepository.loadContacts()
            if (contacts.isEmpty()) {
                _message.value = "No contacts available to export"
                return@launch
            }
            try {
                val file = com.example.domain.usecase.VCardHelper.exportToCacheFile(context, contacts)
                val intent = com.example.domain.usecase.VCardHelper.createShareIntent(context, file)
                onShareIntentReady(intent)
            } catch (e: Exception) {
                _message.value = "Export failed: ${e.localizedMessage}"
            }
        }
    }

    fun importContactsFromUri(context: Context, uri: Uri, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val text = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: ""
                val parsed = com.example.domain.usecase.VCardHelper.parseVCard(text)
                if (parsed.isEmpty()) {
                    _message.value = "No valid contacts found in selected .vcf file"
                    return@launch
                }
                var imported = 0
                for (item in parsed) {
                    val ok = contactsRepository.createContact(
                        firstName = item.firstName,
                        lastName = item.lastName,
                        phone = item.phone,
                        phoneType = item.phoneType,
                        email = item.email,
                        organization = item.organization
                    )
                    if (ok) imported++
                }
                checkForDuplicates()
                _message.value = "Imported $imported contacts from vCard file"
                onComplete()
            } catch (e: Exception) {
                _message.value = "Import failed: ${e.localizedMessage}"
            }
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
