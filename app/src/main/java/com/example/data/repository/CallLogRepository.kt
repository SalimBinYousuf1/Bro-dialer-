package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.CallLog
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallLogRepository(private val context: Context) {

    suspend fun loadCallLogs(): List<CallRecord> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallRecord>()

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_PHOTO_URI,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE,
            CallLog.Calls.IS_READ
        )

        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use { c ->
                val idIdx = c.getColumnIndex(CallLog.Calls._ID)
                val numberIdx = c.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIdx = c.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val photoIdx = c.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)
                val dateIdx = c.getColumnIndex(CallLog.Calls.DATE)
                val durationIdx = c.getColumnIndex(CallLog.Calls.DURATION)
                val typeIdx = c.getColumnIndex(CallLog.Calls.TYPE)
                val isReadIdx = c.getColumnIndex(CallLog.Calls.IS_READ)

                while (c.moveToNext()) {
                    val id = if (idIdx != -1) c.getLong(idIdx) else 0L
                    val number = if (numberIdx != -1) c.getString(numberIdx) ?: "" else ""
                    val callerName = if (nameIdx != -1) c.getString(nameIdx) else null
                    val photo = if (photoIdx != -1) c.getString(photoIdx) else null
                    val date = if (dateIdx != -1) c.getLong(dateIdx) else 0L
                    val duration = if (durationIdx != -1) c.getLong(durationIdx) else 0L
                    val rawType = if (typeIdx != -1) c.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE
                    val isRead = if (isReadIdx != -1) c.getInt(isReadIdx) == 1 else true

                    val callType = when (rawType) {
                        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
                        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                        CallLog.Calls.VOICEMAIL_TYPE -> CallType.VOICEMAIL
                        else -> CallType.INCOMING
                    }

                    list.add(
                        CallRecord(
                            id = id,
                            number = number,
                            callerName = callerName,
                            photoUri = photo,
                            date = date,
                            durationSeconds = duration,
                            type = callType,
                            isRead = isRead
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Permission or system error handled gracefully
        }

        list
    }

    suspend fun deleteCallLog(callId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(CallLog.Calls.CONTENT_URI, callId)
            val deleted = context.contentResolver.delete(uri, null, null)
            deleted > 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteCallLogs(callIds: Set<Long>): Boolean = withContext(Dispatchers.IO) {
        if (callIds.isEmpty()) return@withContext true
        try {
            val selection = "${CallLog.Calls._ID} IN (${callIds.joinToString(",")})"
            val count = context.contentResolver.delete(CallLog.Calls.CONTENT_URI, selection, null)
            count > 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun clearAllCallLogs(): Boolean = withContext(Dispatchers.IO) {
        try {
            val count = context.contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null)
            count >= 0
        } catch (_: Exception) {
            false
        }
    }
}
