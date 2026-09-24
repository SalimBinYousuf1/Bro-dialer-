package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RecentlyDeletedContact
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyDeletedContactDao {

    @Query("SELECT * FROM recently_deleted_contacts ORDER BY deletedTimestamp DESC")
    fun getAll(): Flow<List<RecentlyDeletedContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: RecentlyDeletedContact): Long

    @Query("DELETE FROM recently_deleted_contacts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recently_deleted_contacts")
    suspend fun clearAll()
}
