package com.example.telephony

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi
import com.example.SalimApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class SalimCallScreeningService : CallScreeningService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val handle = callDetails.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""

        // Only screen incoming calls
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondAllowed(callDetails)
            return
        }

        scope.launch {
            try {
                val app = SalimApplication.instance
                val blockedRepo = app.blockedRepository
                val settings = app.preferencesManager.settingsFlow.firstOrNull()

                val isBlocked = if (rawNumber.isNotBlank()) {
                    blockedRepo.isNumberBlocked(rawNumber)
                } else false

                val isUnknown = rawNumber.isBlank() || isUnknownCaller(rawNumber)
                val blockUnknown = settings?.autoRecordUnknown ?: false

                if (isBlocked) {
                    val response = CallResponse.Builder()
                        .setDisallowCall(true)
                        .setRejectCall(true)
                        .setSkipCallLog(false)
                        .setSkipNotification(true)
                        .build()
                    respondToCall(callDetails, response)
                } else {
                    respondAllowed(callDetails)
                }
            } catch (_: Exception) {
                respondAllowed(callDetails)
            }
        }
    }

    private suspend fun isUnknownCaller(number: String): Boolean {
        return try {
            val contact = SalimApplication.instance.contactsRepository.findContactByNumberDirect(number)
            contact == null
        } catch (_: Exception) {
            false
        }
    }

    private fun respondAllowed(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)
    }
}
