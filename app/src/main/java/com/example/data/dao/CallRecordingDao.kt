package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CallRecording
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordingDao {

    @Query("SELECT * FROM call_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE phoneNumber = :number ORDER BY timestamp DESC")
    fun getRecordingsForNumber(number: String): Flow<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): CallRecording?

    @Query("SELECT * FROM call_recordings WHERE callId = :callId LIMIT 1")
    suspend fun getRecordingByCallId(callId: String): CallRecording?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: CallRecording): Long

    @Delete
    suspend fun deleteRecording(recording: CallRecording)

    @Query("DELETE FROM call_recordings WHERE id = :id")
    suspend fun deleteRecordingById(id: Long)
}
