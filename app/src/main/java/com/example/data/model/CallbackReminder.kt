package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "callback_reminders")
data class CallbackReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val phoneNumber: String,
    val contactName: String,
    val remindTimeMillis: Long,
    val note: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
