package com.example.data.repository

import com.example.data.dao.RecentlyDeletedContactDao
import com.example.data.model.ContactItem
import com.example.data.model.RecentlyDeletedContact
import kotlinx.coroutines.flow.Flow

class RecentlyDeletedRepository(
    private val dao: RecentlyDeletedContactDao
) {
    val allDeleted: Flow<List<RecentlyDeletedContact>> = dao.getAll()

    suspend fun recordDeleted(contact: ContactItem) {
        val numbersStr = contact.numbers.joinToString(",") { it.number }
        val emailStr = contact.emails.firstOrNull() ?: ""
        dao.insert(
            RecentlyDeletedContact(
                originalContactId = contact.id,
                name = contact.name,
                phoneNumbers = numbersStr,
                email = emailStr,
                organization = contact.organization ?: "",
                photoUri = contact.photoUri
            )
        )
    }

    suspend fun removePermanently(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
