package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.SalimDatabase
import com.example.data.model.ContactCustomization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ContactCustomizationRepository(private val context: Context) {

    private val dao = SalimDatabase.getDatabase(context).contactCustomizationDao()

    fun getCustomization(contactId: Long): Flow<ContactCustomization?> {
        return dao.getCustomization(contactId)
    }

    suspend fun getCustomizationDirect(contactId: Long): ContactCustomization? = withContext(Dispatchers.IO) {
        dao.getCustomizationDirect(contactId)
    }

    suspend fun setRingtone(contactId: Long, ringtoneUri: String?, ringtoneTitle: String?) = withContext(Dispatchers.IO) {
        val existing = dao.getCustomizationDirect(contactId) ?: ContactCustomization(contactId = contactId)
        dao.saveCustomization(
            existing.copy(
                ringtoneUri = ringtoneUri,
                ringtoneTitle = ringtoneTitle
            )
        )
    }

    suspend fun setCallBackground(contactId: Long, sourceUri: Uri?) = withContext(Dispatchers.IO) {
        val existing = dao.getCustomizationDirect(contactId) ?: ContactCustomization(contactId = contactId)
        if (sourceUri == null) {
            dao.saveCustomization(existing.copy(callBackgroundUri = null))
            return@withContext
        }

        try {
            // Copy image to private internal files directory for persistent reliable access
            val destFile = File(context.filesDir, "contact_bg_${contactId}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            dao.saveCustomization(existing.copy(callBackgroundUri = destFile.absolutePath))
        } catch (_: Exception) {
            dao.saveCustomization(existing.copy(callBackgroundUri = sourceUri.toString()))
        }
    }

    suspend fun setDefaultSim(contactId: Long, simId: Int) = withContext(Dispatchers.IO) {
        val existing = dao.getCustomizationDirect(contactId) ?: ContactCustomization(contactId = contactId)
        dao.saveCustomization(existing.copy(defaultSimId = simId))
    }
}
