package com.example.domain.usecase

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import java.util.Locale

object PhoneNumberHelper {

    fun normalizeNumber(number: String): String {
        return number.replace(Regex("[^0-9+]"), "")
    }

    fun isValidPhoneNumber(number: String): Boolean {
        val clean = number.trim()
        if (clean.length < 3) return false
        // Valid characters in a dial string include digits, +, *, #
        return clean.matches(Regex("^[+]?[0-9*#,\\;\\s\\-().]{3,30}$"))
    }

    fun formatForDisplay(number: String, defaultCountryIso: String = Locale.getDefault().country): String {
        val clean = number.trim()
        if (clean.isBlank()) return ""
        val formatted = PhoneNumberUtils.formatNumber(clean, defaultCountryIso.ifBlank { "US" })
        return formatted ?: clean
    }

    fun areNumbersEqual(num1: String, num2: String): Boolean {
        val s1 = num1.trim()
        val s2 = num2.trim()
        if (s1.isBlank() || s2.isBlank()) return false
        if (s1 == s2) return true

        // Use Android system PhoneNumberUtils
        try {
            if (PhoneNumberUtils.compare(s1, s2)) return true
        } catch (_: Exception) {}

        // Fallback: compare digit-only strings
        val d1 = s1.filter { it.isDigit() }
        val d2 = s2.filter { it.isDigit() }
        if (d1.isEmpty() || d2.isEmpty()) return false
        if (d1 == d2) return true

        // Match suffix of 7, 8, 9, or 10 digits (national number without country code)
        val minLen = minOf(d1.length, d2.length)
        if (minLen >= 7) {
            val compareLen = minOf(minLen, 10)
            val sub1 = d1.takeLast(compareLen)
            val sub2 = d2.takeLast(compareLen)
            if (sub1 == sub2) return true
        }

        return false
    }

    fun findContactForNumber(contacts: List<com.example.data.model.ContactItem>, queryNumber: String): com.example.data.model.ContactItem? {
        val q = queryNumber.trim()
        if (q.isBlank()) return null
        return contacts.firstOrNull { contact ->
            contact.numbers.any { phone ->
                areNumbersEqual(phone.number, q) || areNumbersEqual(phone.normalizedNumber, q)
            }
        }
    }

    fun isEmergencyNumber(context: Context, number: String): Boolean {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (tm != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                tm.isEmergencyNumber(number)
            } else {
                PhoneNumberUtils.isEmergencyNumber(number)
            }
        } catch (_: Exception) {
            false
        }
    }
}
