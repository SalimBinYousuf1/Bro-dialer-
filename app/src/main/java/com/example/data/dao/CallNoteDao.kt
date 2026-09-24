package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CallNote
import kotlinx.coroutines.flow.Flow

@Dao
interface CallNoteDao {

    @Query("SELECT * FROM call_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<CallNote>>

    @Query("SELECT * FROM call_notes WHERE callNumber = :number ORDER BY timestamp DESC")
    fun getNotesForNumber(number: String): Flow<List<CallNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CallNote): Long

    @Query("DELETE FROM call_notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)
}
