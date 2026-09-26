package com.example.data.repository

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.model.ContactItem
import com.example.data.model.ContactPhoneNumber
import com.example.domain.usecase.PhoneNumberHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {

    fun getContactsStream(): Flow<List<ContactItem>> = flow {
        emit(loadContacts())
    }.flowOn(Dispatchers.IO)

    suspend fun loadContacts(): List<ContactItem> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }

        try {
            val contactsMap = mutableMapOf<Long, ContactItemBuilder>()

            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.Contacts.STARRED
            )

            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC"
            )

            cursor?.use { c ->
                val idIdx = c.getColumnIndex(ContactsContract.Contacts._ID)
                val lookupIdx = c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
                val nameIdx = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val photoIdx = c.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
                val starredIdx = c.getColumnIndex(ContactsContract.Contacts.STARRED)

                while (c.moveToNext()) {
                    val id = c.getLong(idIdx)
                    val lookup = if (lookupIdx != -1) c.getString(lookupIdx) ?: "" else ""
                    val name = if (nameIdx != -1) c.getString(nameIdx) ?: "Unknown" else "Unknown"
                    val photo = if (photoIdx != -1) c.getString(photoIdx) else null
                    val starred = if (starredIdx != -1) c.getInt(starredIdx) == 1 else false

                    contactsMap[id] = ContactItemBuilder(
                        id = id,
                        lookupKey = lookup,
                        name = name,
                        photoUri = photo,
                        isFavorite = starred
                    )
                }
            }

        // Now load phone numbers in bulk
        val phoneProjection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
        )

        val phoneCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            phoneProjection,
            null,
            null,
            null
        )

        phoneCursor?.use { pc ->
            val contactIdIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numberIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val normIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)
            val typeIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
            val labelIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
            val isPrimaryIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)

            while (pc.moveToNext()) {
                val contactId = pc.getLong(contactIdIdx)
                val builder = contactsMap[contactId] ?: continue

                val rawNumber = if (numberIdx != -1) pc.getString(numberIdx) ?: "" else ""
                val normalized = if (normIdx != -1) pc.getString(normIdx) ?: "" else ""
                val type = if (typeIdx != -1) pc.getInt(typeIdx) else ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                val customLabel = if (labelIdx != -1) pc.getString(labelIdx) else null
                val isPrimary = if (isPrimaryIdx != -1) pc.getInt(isPrimaryIdx) == 1 else false

                val typeStr = when (type) {
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "Mobile"
                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
                    ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> "Main"
                    ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM -> customLabel ?: "Custom"
                    else -> "Other"
                }

                builder.numbers.add(
                    ContactPhoneNumber(
                        number = rawNumber,
                        normalizedNumber = if (normalized.isNotBlank()) normalized else PhoneNumberHelper.normalizeNumber(rawNumber),
                        type = typeStr,
                        isPrimary = isPrimary
                    )
                )
            }
        }

        contactsMap.values.map { it.build() }.sortedBy { it.name.lowercase() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getContactDetails(contactId: Long): ContactItem? = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext null
        }

        try {
            val all = loadContacts()
            val found = all.find { it.id == contactId } ?: return@withContext null

            // Load emails, org, note
            val emails = mutableListOf<String>()
            var organization: String? = null
            var note: String? = null

            val emailCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                arrayOf(contactId.toString()),
                null
            )
            emailCursor?.use {
                val idx = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                while (it.moveToNext()) {
                    if (idx != -1) emails.add(it.getString(idx) ?: "")
                }
            }

            // Load organization
            val orgCursor = context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Organization.COMPANY),
                "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                arrayOf(contactId.toString(), ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                null
            )
            orgCursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)
                    if (idx != -1) organization = it.getString(idx)
                }
            }

            found.copy(emails = emails.filter { it.isNotBlank() }, organization = organization, note = note)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun toggleFavorite(contactId: Long, isFavorite: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(ContactsContract.Contacts.STARRED, if (isFavorite) 1 else 0)
            }
            val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            val updated = context.contentResolver.update(uri, values, null, null)
            updated > 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteContact(contactId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            val count = context.contentResolver.delete(uri, null, null)
            count > 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun createContact(
        firstName: String,
        lastName: String,
        phone: String,
        phoneType: String = "Mobile",
        email: String = "",
        organization: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val ops = ArrayList<ContentProviderOperation>()
            val rawContactInsertIndex = ops.size

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            val fullName = "$firstName $lastName".trim()
            if (fullName.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName.trim())
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName.trim())
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName)
                        .build()
                )
            }

            if (phone.isNotBlank()) {
                val typeVal = when (phoneType) {
                    "Home" -> ContactsContract.CommonDataKinds.Phone.TYPE_HOME
                    "Work" -> ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                    else -> ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                }
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.trim())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, typeVal)
                        .build()
                )
            }

            if (email.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, email.trim())
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .build()
                )
            }

            if (organization.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, organization.trim())
                        .build()
                )
            }

            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun updateContact(
        contactId: Long,
        firstName: String,
        lastName: String,
        phone: String,
        phoneType: String = "Mobile",
        email: String = "",
        organization: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Find raw contact ID
            val rawContactId = getRawContactId(contactId) ?: return@withContext false

            val ops = ArrayList<ContentProviderOperation>()

            // Clear existing data rows for name, phone, email, organization
            val mimetypesToDelete = arrayOf(
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
            )

            for (mimetype in mimetypesToDelete) {
                ops.add(
                    ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                            "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                            arrayOf(rawContactId.toString(), mimetype)
                        )
                        .build()
                )
            }

            // Insert updated name
            val fullName = "$firstName $lastName".trim()
            if (fullName.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName.trim())
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName.trim())
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName)
                        .build()
                )
            }

            // Insert updated phone
            if (phone.isNotBlank()) {
                val typeVal = when (phoneType) {
                    "Home" -> ContactsContract.CommonDataKinds.Phone.TYPE_HOME
                    "Work" -> ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                    else -> ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                }
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.trim())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, typeVal)
                        .build()
                )
            }

            // Insert updated email
            if (email.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, email.trim())
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .build()
                )
            }

            // Insert updated organization
            if (organization.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, organization.trim())
                        .build()
                )
            }

            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun getRawContactId(contactId: Long): Long? {
        val cursor = context.contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts._ID),
            "${ContactsContract.RawContacts.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )
        return cursor?.use {
            if (it.moveToFirst()) it.getLong(0) else null
        }
    }

    private data class ContactItemBuilder(
        val id: Long,
        val lookupKey: String,
        val name: String,
        val photoUri: String?,
        val isFavorite: Boolean,
        val numbers: MutableList<ContactPhoneNumber> = mutableListOf()
    ) {
        fun build() = ContactItem(
            id = id,
            lookupKey = lookupKey,
            name = name,
            photoUri = photoUri,
            numbers = numbers,
            isFavorite = isFavorite
        )
    }
}
