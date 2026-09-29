package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_recordings")
data class CallRecording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val callId: String = "",
    val phoneNumber: String,
    val displayName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val filePath: String,
    val fileSizeBytes: Long = 0
) {
    val formattedDuration: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }

    val formattedSize: String
        get() {
            return when {
                fileSizeBytes >= 1024 * 1024 -> "%.1f MB".format(fileSizeBytes / (1024.0 * 1024.0))
                fileSizeBytes >= 1024 -> "%.0f KB".format(fileSizeBytes / 1024.0)
                else -> "$fileSizeBytes B"
            }
        }
}
