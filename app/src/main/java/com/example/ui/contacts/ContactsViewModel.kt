package com.example.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.model.ContactItem
import com.example.data.repository.BlockedRepository
import com.example.data.repository.ContactsRepository
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
    private val telecomRepository: TelecomRepository = SalimApplication.instance.telecomRepository
) : ViewModel() {

    private val _rawContacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val rawContacts: StateFlow<List<ContactItem>> = _rawContacts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentContact = MutableStateFlow<ContactItem?>(null)
    val currentContact: StateFlow<ContactItem?> = _currentContact.asStateFlow()

    val filteredContacts: StateFlow<List<ContactItem>> = combine(
        _rawContacts,
        _searchQuery
    ) { contacts, query ->
        if (query.isBlank()) {
            contacts
        } else {
            val q = query.trim().lowercase()
            contacts.filter { contact ->
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
            _currentContact.value = contactsRepository.getContactDetails(contactId)
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
            val success = contactsRepository.deleteContact(contactId)
            if (success) {
                _rawContacts.value = _rawContacts.value.filter { it.id != contactId }
                if (_currentContact.value?.id == contactId) {
                    _currentContact.value = null
                }
                onDone()
            }
        }
    }

    fun createContact(
        firstName: String,
        lastName: String,
        phone: String,
        type: String = "Mobile",
        email: String = "",
        organization: String = "",
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val ok = contactsRepository.createContact(firstName, lastName, phone, type, email, organization)
            if (ok) {
                loadContacts()
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
