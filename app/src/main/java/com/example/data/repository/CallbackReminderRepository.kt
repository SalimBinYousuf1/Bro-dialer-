package com.example.data.repository

import com.example.data.local.CallbackReminderDao
import com.example.data.model.CallbackReminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CallbackReminderRepository(private val dao: CallbackReminderDao) {

    val pendingReminders: Flow<List<CallbackReminder>> = dao.getAllPendingReminders()

    suspend fun scheduleReminder(
        phoneNumber: String,
        contactName: String,
        remindTimeMillis: Long,
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val reminder = CallbackReminder(
            phoneNumber = phoneNumber.trim(),
            contactName = contactName.trim().ifBlank { phoneNumber.trim() },
            remindTimeMillis = remindTimeMillis,
            note = note.trim()
        )
        dao.insert(reminder)
    }

    suspend fun getUpcomingPending(now: Long = System.currentTimeMillis()): List<CallbackReminder> = withContext(Dispatchers.IO) {
        dao.getUpcomingPendingReminders(now)
    }

    suspend fun markCompleted(id: Long) = withContext(Dispatchers.IO) {
        dao.markCompleted(id)
    }

    suspend fun deleteReminder(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun getById(id: Long): CallbackReminder? = withContext(Dispatchers.IO) {
        dao.getById(id)
    }
}
