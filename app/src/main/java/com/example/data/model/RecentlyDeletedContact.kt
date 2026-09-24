package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_deleted_contacts")
data class RecentlyDeletedContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalContactId: Long,
    val name: String,
    val phoneNumbers: String, // comma-separated or primary number
    val email: String = "",
    val organization: String = "",
    val photoUri: String? = null,
    val deletedTimestamp: Long = System.currentTimeMillis()
)
