package com.example.data.repository

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import com.example.domain.usecase.PhoneNumberHelper

class TelecomRepository(private val context: Context) {

    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager

    fun isDefaultDialer(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) ?: false
        } else {
            telecomManager?.defaultDialerPackage == context.packageName
        }
    }

    fun getDefaultDialerIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        } else {
            @Suppress("DEPRECATION")
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
            }
        }
    }

    fun makeCall(rawNumber: String, simId: Int = -1): Boolean {
        val cleanNumber = PhoneNumberHelper.normalizeNumber(rawNumber)
        if (cleanNumber.isBlank()) return false

        val uri = Uri.fromParts("tel", cleanNumber, null)

        // Android Telecom emergency call handling: route directly to system
        if (PhoneNumberHelper.isEmergencyNumber(context, cleanNumber)) {
            val emergencyIntent = Intent(Intent.ACTION_CALL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return try {
                context.startActivity(emergencyIntent)
                true
            } catch (_: Exception) {
                try {
                    val dialFallback = Intent(Intent.ACTION_DIAL, uri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(dialFallback)
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }

        // Launch Salim CallActivity for regular calls
        com.example.telephony.CallManager.initiateOutgoingCall(context, cleanNumber)

        val hasCallPhone = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val isDefault = isDefaultDialer()

        return try {
            if (isDefault || hasCallPhone) {
                val callIntent = Intent(Intent.ACTION_CALL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (simId > 0) {
                        try {
                            val accounts = telecomManager?.callCapablePhoneAccounts
                            if (!accounts.isNullOrEmpty()) {
                                val accountIndex = (simId - 1).coerceIn(0, accounts.size - 1)
                                putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accounts[accountIndex])
                            }
                        } catch (_: Exception) {}
                    }
                }
                context.startActivity(callIntent)
                true
            } else {
                val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
                true
            }
        } catch (_: Exception) {
            try {
                val dialFallback = Intent(Intent.ACTION_DIAL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialFallback)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun sendDirectSms(rawNumber: String, message: String): Boolean {
        val cleanNumber = PhoneNumberHelper.normalizeNumber(rawNumber)
        if (cleanNumber.isBlank() || message.isBlank()) return false
        return try {
            val smsManager: android.telephony.SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(android.telephony.SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                android.telephony.SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(cleanNumber, null, message, null, null)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openSms(number: String) {
        val uri = Uri.parse("smsto:${PhoneNumberHelper.normalizeNumber(number)}")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
