package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_notes")
data class CallNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callNumber: String,
    val contactName: String?,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)
