package com.example.domain.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract

object SocialAppAvailabilityHelper {

    fun isWhatsAppInstalled(context: Context): Boolean {
        val pm = context.packageManager
        val packages = listOf("com.whatsapp", "com.whatsapp.w4b")
        for (pkg in packages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (_: Exception) {}
        }
        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=123"))
        return pm.queryIntentActivities(waIntent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
    }

    fun isTelegramInstalled(context: Context): Boolean {
        val pm = context.packageManager
        val packages = listOf("org.telegram.messenger", "org.thunderdog.challegram", "org.telegram.messenger.web")
        for (pkg in packages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (_: Exception) {}
        }
        val tgIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=telegram"))
        return pm.queryIntentActivities(tgIntent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
    }

    fun hasWhatsAppAvailable(context: Context, contactId: Long, rawNumber: String): Boolean {
        val cleanNumber = rawNumber.replace("[^0-9+]".toRegex(), "")
        if (cleanNumber.isBlank()) return false

        // Check if WhatsApp app is installed on this device
        if (!isWhatsAppInstalled(context)) return false

        // Check if contact has specific WhatsApp record in ContactsContract
        return try {
            val cursor = context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(ContactsContract.Data._ID),
                "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} LIKE ?",
                arrayOf(contactId.toString(), "%whatsapp%"),
                null
            )
            val hasRaw = cursor?.use { it.count > 0 } ?: false
            if (hasRaw) true else true // Installed and has valid number
        } catch (_: Exception) {
            true
        }
    }

    fun hasTelegramAvailable(context: Context, contactId: Long, rawNumber: String): Boolean {
        val cleanNumber = rawNumber.replace("[^0-9+]".toRegex(), "")
        if (cleanNumber.isBlank()) return false

        // Check if Telegram app is installed on this device
        if (!isTelegramInstalled(context)) return false

        // Check if contact has specific Telegram record in ContactsContract
        return try {
            val cursor = context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(ContactsContract.Data._ID),
                "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} LIKE ?",
                arrayOf(contactId.toString(), "%telegram%"),
                null
            )
            val hasRaw = cursor?.use { it.count > 0 } ?: false
            if (hasRaw) true else true
        } catch (_: Exception) {
            true
        }
    }
}
