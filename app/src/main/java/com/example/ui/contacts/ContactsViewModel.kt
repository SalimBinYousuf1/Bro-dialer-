package com.example.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.model.CallRecord
import com.example.data.model.ContactAvatar
import com.example.data.model.ContactItem
import com.example.data.repository.BlockedRepository
import com.example.data.repository.CallLogRepository
import com.example.data.repository.ContactAvatarRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.RecentlyDeletedRepository
import com.example.data.repository.TelecomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val contactsRepository: ContactsRepository = SalimApplication.instance.contactsRepository,
    private val blockedRepository: BlockedRepository = SalimApplication.instance.blockedRepository,
    private val telecomRepository: TelecomRepository = SalimApplication.instance.telecomRepository,
    private val contactAvatarRepository: ContactAvatarRepository = SalimApplication.instance.contactAvatarRepository,
    private val recentlyDeletedRepository: RecentlyDeletedRepository = SalimApplication.instance.recentlyDeletedRepository,
    private val callLogRepository: CallLogRepository = SalimApplication.instance.callLogRepository
) : ViewModel() {

    private val _rawContacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val rawContacts: StateFlow<List<ContactItem>> = _rawContacts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentContact = MutableStateFlow<ContactItem?>(null)
    val currentContact: StateFlow<ContactItem?> = _currentContact.asStateFlow()

    val avatars: StateFlow<List<ContactAvatar>> = contactAvatarRepository.allAvatars
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredContacts: StateFlow<List<ContactItem>> = combine(
        _rawContacts,
        _searchQuery,
        avatars
    ) { contacts, query, avatarList ->
        val avatarMap = avatarList.associate { it.contactId to it.avatarUri }
        val mappedContacts = contacts.map { contact ->
            val customAvatar = avatarMap[contact.id]
            if (customAvatar != null) contact.copy(photoUri = customAvatar) else contact
        }

        if (query.isBlank()) {
            mappedContacts
        } else {
            val q = query.trim().lowercase()
            mappedContacts.filter { contact ->
                contact.name.lowercase().contains(q) ||
                    contact.numbers.any { it.number.contains(q) || it.normalizedNumber.contains(q) } ||
                    contact.emails.any { it.lowercase().contains(q) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            val list = contactsRepository.loadContacts()
            _rawContacts.value = list
            _isLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun loadContactDetails(contactId: Long) {
        viewModelScope.launch {
            val item = contactsRepository.getContactDetails(contactId)
            val customAvatar = contactAvatarRepository.getAvatarSync(contactId)
            _currentContact.value = if (customAvatar != null && item != null) {
                item.copy(photoUri = customAvatar)
            } else {
                item
            }
        }
    }

    fun loadContactById(contactId: Long) = loadContactDetails(contactId)

    fun updateAvatar(contactId: Long, avatarUri: String?) = setContactAvatar(contactId, avatarUri)

    fun setContactAvatar(contactId: Long, avatarUri: String?) {
        viewModelScope.launch {
            if (avatarUri != null) {
                contactAvatarRepository.setAvatar(contactId, avatarUri)
            } else {
                contactAvatarRepository.deleteAvatar(contactId)
            }
            loadContactDetails(contactId)
            loadContacts()
        }
    }

    fun toggleFavorite(contact: ContactItem) {
        viewModelScope.launch {
            val newFav = !contact.isFavorite
            val success = contactsRepository.toggleFavorite(contact.id, newFav)
            if (success) {
                _rawContacts.value = _rawContacts.value.map {
                    if (it.id == contact.id) it.copy(isFavorite = newFav) else it
                }
                if (_currentContact.value?.id == contact.id) {
                    _currentContact.value = _currentContact.value?.copy(isFavorite = newFav)
                }
            }
        }
    }

    fun deleteContact(contactId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            val contact = _rawContacts.value.find { it.id == contactId } ?: _currentContact.value
            if (contact != null) {
                recentlyDeletedRepository.recordDeleted(contact)
            }
            val success = contactsRepository.deleteContact(contactId)
            if (success) {
                contactAvatarRepository.deleteAvatar(contactId)
                _rawContacts.value = _rawContacts.value.filter { it.id != contactId }
                if (_currentContact.value?.id == contactId) {
                    _currentContact.value = null
                }
                onDone()
            }
        }
    }

    suspend fun loadCallLogsForContact(numbers: List<String>): List<CallRecord> {
        return callLogRepository.getCallLogsForNumbers(numbers)
    }

    fun createContact(
        firstName: String,
        lastName: String,
        phone: String,
        type: String = "Mobile",
        email: String = "",
        organization: String = "",
        avatarUri: String? = null,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val ok = contactsRepository.createContact(firstName, lastName, phone, type, email, organization)
            if (ok) {
                loadContacts()
                if (avatarUri != null) {
                    // Match newly created contact by phone or name
                    val all = contactsRepository.loadContacts()
                    val created = all.find { it.name.trim() == "$firstName $lastName".trim() }
                    if (created != null) {
                        contactAvatarRepository.setAvatar(created.id, avatarUri)
                        loadContacts()
                    }
                }
            }
            onResult(ok)
        }
    }

    fun blockContact(contact: ContactItem, onResult: () -> Unit) {
        viewModelScope.launch {
            contact.numbers.forEach { phone ->
                blockedRepository.blockNumber(phone.number, contact.name)
            }
            onResult()
        }
    }

    fun makeCall(number: String) {
        telecomRepository.makeCall(number)
    }

    fun sendSms(number: String) {
        telecomRepository.openSms(number)
    }
}
