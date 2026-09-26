package com.example.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ContactItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VCardHelper {

    data class ParsedContact(
        val firstName: String,
        val lastName: String,
        val phone: String,
        val phoneType: String = "Mobile",
        val email: String = "",
        val organization: String = ""
    ) {
        val fullName: String
            get() = "$firstName $lastName".trim()
    }

    fun generateVCard(contacts: List<ContactItem>): String {
        val sb = StringBuilder()
        for (contact in contacts) {
            sb.append("BEGIN:VCARD\r\n")
            sb.append("VERSION:3.0\r\n")
            sb.append("FN:${contact.name.trim()}\r\n")

            val nameParts = contact.name.trim().split(" ", limit = 2)
            val first = nameParts.getOrElse(0) { "" }
            val last = nameParts.getOrElse(1) { "" }
            sb.append("N:$last;$first;;;\r\n")

            for (phone in contact.numbers) {
                val num = phone.number.trim()
                if (num.isNotBlank()) {
                    val type = when (phone.type.lowercase(Locale.ROOT)) {
                        "work" -> "WORK"
                        "home" -> "HOME"
                        else -> "CELL"
                    }
                    sb.append("TEL;TYPE=$type:$num\r\n")
                }
            }
            sb.append("END:VCARD\r\n")
        }
        return sb.toString()
    }

    fun exportToCacheFile(context: Context, contacts: List<ContactItem>): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "salim_contacts_$timestamp.vcf")
        file.writeText(generateVCard(contacts))
        return file
    }

    fun createShareIntent(context: Context, file: File): Intent {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/x-vcard"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun saveLocalBackup(context: Context, contacts: List<ContactItem>): Boolean {
        return try {
            val file = File(context.filesDir, "contacts_backup.vcf")
            file.writeText(generateVCard(contacts))
            val metaFile = File(context.filesDir, "contacts_backup_meta.txt")
            metaFile.writeText(System.currentTimeMillis().toString())
            true
        } catch (_: Exception) {
            false
        }
    }

    fun hasLocalBackup(context: Context): Boolean {
        val file = File(context.filesDir, "contacts_backup.vcf")
        return file.exists() && file.length() > 0
    }

    fun getLastBackupTime(context: Context): String? {
        val metaFile = File(context.filesDir, "contacts_backup_meta.txt")
        if (!metaFile.exists()) return null
        return try {
            val ms = metaFile.readText().trim().toLong()
            SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(ms))
        } catch (_: Exception) {
            null
        }
    }

    fun readLocalBackup(context: Context): List<ParsedContact> {
        val file = File(context.filesDir, "contacts_backup.vcf")
        if (!file.exists()) return emptyList()
        return parseVCard(file.readText())
    }

    fun parseVCard(vcardText: String): List<ParsedContact> {
        val result = mutableListOf<ParsedContact>()
        val cards = vcardText.split("BEGIN:VCARD")

        for (rawCard in cards) {
            if (rawCard.isBlank()) continue
            val lines = rawCard.lines().map { it.trim() }

            var fn = ""
            var givenName = ""
            var familyName = ""
            var phone = ""
            var phoneType = "Mobile"
            var email = ""
            var org = ""

            for (line in lines) {
                when {
                    line.startsWith("FN:", ignoreCase = true) -> {
                        fn = line.substring(3).trim()
                    }
                    line.startsWith("N:", ignoreCase = true) -> {
                        val nParts = line.substring(2).split(";")
                        familyName = nParts.getOrElse(0) { "" }.trim()
                        givenName = nParts.getOrElse(1) { "" }.trim()
                    }
                    line.startsWith("TEL", ignoreCase = true) -> {
                        val colonIdx = line.indexOf(':')
                        if (colonIdx != -1) {
                            val num = line.substring(colonIdx + 1).trim()
                            if (phone.isEmpty()) {
                                phone = num
                                if (line.contains("WORK", ignoreCase = true)) phoneType = "Work"
                                else if (line.contains("HOME", ignoreCase = true)) phoneType = "Home"
                            }
                        }
                    }
                    line.startsWith("EMAIL", ignoreCase = true) -> {
                        val colonIdx = line.indexOf(':')
                        if (colonIdx != -1 && email.isEmpty()) {
                            email = line.substring(colonIdx + 1).trim()
                        }
                    }
                    line.startsWith("ORG:", ignoreCase = true) -> {
                        org = line.substring(4).replace(";", " ").trim()
                    }
                }
            }

            val finalFirst = if (givenName.isNotBlank()) givenName else {
                val parts = fn.split(" ", limit = 2)
                parts.getOrElse(0) { "" }
            }
            val finalLast = if (familyName.isNotBlank()) familyName else {
                val parts = fn.split(" ", limit = 2)
                parts.getOrElse(1) { "" }
            }

            if ((finalFirst.isNotBlank() || finalLast.isNotBlank() || fn.isNotBlank()) && phone.isNotBlank()) {
                result.add(
                    ParsedContact(
                        firstName = if (finalFirst.isNotBlank()) finalFirst else fn,
                        lastName = finalLast,
                        phone = phone,
                        phoneType = phoneType,
                        email = email,
                        organization = org
                    )
                )
            }
        }

        return result
    }
}
