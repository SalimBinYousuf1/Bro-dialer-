package com.example.data.repository

import com.example.data.dao.ContactAvatarDao
import com.example.data.model.ContactAvatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ContactAvatarRepository(private val avatarDao: ContactAvatarDao) {

    val allAvatars: Flow<List<ContactAvatar>> = avatarDao.getAllAvatars().flowOn(Dispatchers.IO)

    fun getAvatar(contactId: Long): Flow<ContactAvatar?> {
        return avatarDao.getAvatar(contactId).flowOn(Dispatchers.IO)
    }

    suspend fun getAvatarSync(contactId: Long): String? = withContext(Dispatchers.IO) {
        avatarDao.getAvatarSync(contactId)?.avatarUri
    }

    suspend fun setAvatar(contactId: Long, avatarUri: String) = withContext(Dispatchers.IO) {
        avatarDao.setAvatar(ContactAvatar(contactId, avatarUri))
    }

    suspend fun deleteAvatar(contactId: Long) = withContext(Dispatchers.IO) {
        avatarDao.deleteAvatar(contactId)
    }
}
