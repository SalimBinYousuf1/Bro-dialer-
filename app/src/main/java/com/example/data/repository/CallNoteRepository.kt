package com.example.data.repository

import com.example.data.dao.CallNoteDao
import com.example.data.model.CallNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class CallNoteRepository(private val noteDao: CallNoteDao) {

    val allNotes: Flow<List<CallNote>> = noteDao.getAllNotes().flowOn(Dispatchers.IO)

    fun getNotesForNumber(number: String): Flow<List<CallNote>> {
        return noteDao.getNotesForNumber(number).flowOn(Dispatchers.IO)
    }

    suspend fun saveNote(number: String, contactName: String?, text: String): Long = withContext(Dispatchers.IO) {
        noteDao.insertNote(
            CallNote(
                callNumber = number,
                contactName = contactName,
                noteText = text
            )
        )
    }

    suspend fun deleteNote(noteId: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(noteId)
    }
}
