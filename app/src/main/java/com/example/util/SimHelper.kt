package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

data class SimAccount(
    val slotIndex: Int, // 0 = SIM 1, 1 = SIM 2
    val simId: Int, // 1-indexed for dialer convention
    val subscriptionId: Int,
    val displayName: String,
    val carrierName: String,
    val phoneAccountHandle: PhoneAccountHandle? = null
)

object SimHelper {

    fun getActiveSims(context: Context): List<SimAccount> {
        val list = mutableListOf<SimAccount>()
        try {
            val subManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            } else null

            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED

            if (subManager != null && hasPermission) {
                val subList: List<SubscriptionInfo>? = subManager.activeSubscriptionInfoList
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                val accounts = try {
                    telecomManager?.callCapablePhoneAccounts
                } catch (_: Exception) {
                    null
                }

                subList?.forEachIndexed { index, info ->
                    val slot = info.simSlotIndex
                    val name = info.displayName?.toString()?.ifBlank { null }
                        ?: info.carrierName?.toString()?.ifBlank { null }
                        ?: "SIM ${slot + 1}"
                    val carrier = info.carrierName?.toString() ?: ""
                    val handle = if (!accounts.isNullOrEmpty() && index < accounts.size) {
                        accounts[index]
                    } else null

                    list.add(
                        SimAccount(
                            slotIndex = slot,
                            simId = slot + 1,
                            subscriptionId = info.subscriptionId,
                            displayName = name,
                            carrierName = carrier,
                            phoneAccountHandle = handle
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // Fallback default if no multi-SIM detected
        if (list.isEmpty()) {
            list.add(
                SimAccount(
                    slotIndex = 0,
                    simId = 1,
                    subscriptionId = -1,
                    displayName = "SIM 1",
                    carrierName = "Default"
                )
            )
        }
        return list
    }
}
