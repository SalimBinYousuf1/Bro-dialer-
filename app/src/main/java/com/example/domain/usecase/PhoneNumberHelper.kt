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
