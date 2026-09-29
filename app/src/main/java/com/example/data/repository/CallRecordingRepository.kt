package com.example.data.repository

import android.content.Context
import com.example.data.dao.CallRecordingDao
import com.example.data.model.CallRecording
import kotlinx.coroutines.flow.Flow
import java.io.File

class CallRecordingRepository(
    private val context: Context,
    private val dao: CallRecordingDao
) {
    fun getAllRecordings(): Flow<List<CallRecording>> = dao.getAllRecordings()

    fun getRecordingsForNumber(number: String): Flow<List<CallRecording>> = dao.getRecordingsForNumber(number)

    suspend fun getRecordingById(id: Long): CallRecording? = dao.getRecordingById(id)

    suspend fun getRecordingByCallId(callId: String): CallRecording? = dao.getRecordingByCallId(callId)

    suspend fun saveRecording(recording: CallRecording): Long = dao.insertRecording(recording)

    suspend fun deleteRecording(recording: CallRecording) {
        try {
            val file = File(recording.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        dao.deleteRecording(recording)
    }

    suspend fun deleteRecordingById(id: Long) {
        val rec = dao.getRecordingById(id)
        if (rec != null) {
            deleteRecording(rec)
        }
    }
}
