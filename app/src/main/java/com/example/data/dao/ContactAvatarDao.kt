package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ContactAvatar
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactAvatarDao {

    @Query("SELECT * FROM contact_avatars WHERE contactId = :contactId LIMIT 1")
    fun getAvatar(contactId: Long): Flow<ContactAvatar?>

    @Query("SELECT * FROM contact_avatars WHERE contactId = :contactId LIMIT 1")
    suspend fun getAvatarSync(contactId: Long): ContactAvatar?

    @Query("SELECT * FROM contact_avatars")
    fun getAllAvatars(): Flow<List<ContactAvatar>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAvatar(avatar: ContactAvatar)

    @Query("DELETE FROM contact_avatars WHERE contactId = :contactId")
    suspend fun deleteAvatar(contactId: Long)
}
