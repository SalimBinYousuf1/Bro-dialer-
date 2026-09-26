package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ContactCustomization
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactCustomizationDao {

    @Query("SELECT * FROM contact_customizations WHERE contactId = :contactId")
    fun getCustomization(contactId: Long): Flow<ContactCustomization?>

    @Query("SELECT * FROM contact_customizations WHERE contactId = :contactId")
    suspend fun getCustomizationDirect(contactId: Long): ContactCustomization?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCustomization(customization: ContactCustomization)

    @Query("DELETE FROM contact_customizations WHERE contactId = :contactId")
    suspend fun deleteCustomization(contactId: Long)
}
