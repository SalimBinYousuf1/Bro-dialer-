package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_numbers")
data class BlockedNumber(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,
    val normalizedNumber: String,
    val contactName: String? = null,
    val blockedAt: Long = System.currentTimeMillis()
)
