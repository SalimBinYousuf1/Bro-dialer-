package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_avatars")
data class ContactAvatar(
    @PrimaryKey val contactId: Long,
    val avatarUri: String // Can be a content://, file://, or "preset:*"
)
