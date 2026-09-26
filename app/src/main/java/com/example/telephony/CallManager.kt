package com.example.telephony

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.TelecomManager
import com.example.data.model.ActiveCallInfo
import com.example.data.model.TelephonyCallState
import com.example.domain.usecase.PhoneNumberHelper
import com.example.ui.call.CallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object CallManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var inCallService: SalimInCallService? = null
    private var activeCall: Call? = null

    private val _currentCallInfo = MutableStateFlow<ActiveCallInfo?>(null)
    val currentCallInfo: StateFlow<ActiveCallInfo?> = _currentCallInfo.asStateFlow()

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call?, state: Int) {
            super.onStateChanged(call, state)
            updateCallState(call)
        }

        override fun onDetailsChanged(call: Call?, details: Call.Details?) {
            super.onDetailsChanged(call, details)
            updateCallState(call)
        }
    }

    fun setInCallService(service: SalimInCallService?) {
        this.inCallService = service
    }

    fun initiateOutgoingCall(context: Context, number: String, name: String = "") {
        val clean = PhoneNumberHelper.normalizeNumber(number)
        _currentCallInfo.value = ActiveCallInfo(
            callId = System.currentTimeMillis().toString(),
            number = clean,
            displayName = name.ifBlank { PhoneNumberHelper.formatForDisplay(clean) },
            state = TelephonyCallState.DIALING,
            isMuted = false,
            isSpeakerOn = false,
            isBluetoothOn = false,
            isOnHold = false
        )
        val intent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
    }

    fun onCallAdded(call: Call, context: Context) {
        activeCall?.unregisterCallback(callCallback)
        activeCall = call
        call.registerCallback(callCallback)
        updateCallState(call)

        // Launch CallActivity
        val intent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
    }

    fun onCallRemoved(call: Call) {
        call.unregisterCallback(callCallback)
        if (activeCall == call) {
            _currentCallInfo.value = _currentCallInfo.value?.copy(state = TelephonyCallState.DISCONNECTED)
            activeCall = null
            // Reset state shortly after disconnect
            Handler(Looper.getMainLooper()).postDelayed({
                _currentCallInfo.value = null
            }, 1500)
        }
    }

    fun onAudioStateChanged(audioState: CallAudioState) {
        val current = _currentCallInfo.value ?: return
        val isMuted = audioState.isMuted
        val isSpeaker = (audioState.route and CallAudioState.ROUTE_SPEAKER) != 0
        val isBluetooth = (audioState.route and CallAudioState.ROUTE_BLUETOOTH) != 0
        _currentCallInfo.value = current.copy(
            isMuted = isMuted,
            isSpeakerOn = isSpeaker,
            isBluetoothOn = isBluetooth
        )
    }

    private fun updateCallState(call: Call?) {
        if (call == null) {
            _currentCallInfo.value = null
            return
        }

        val handle = call.details?.handle
        val schemeSpecific = handle?.schemeSpecificPart ?: ""
        val callerName = call.details?.callerDisplayName ?: ""
        val stateEnum = when (call.state) {
            Call.STATE_RINGING -> TelephonyCallState.RINGING
            Call.STATE_DIALING, Call.STATE_CONNECTING -> TelephonyCallState.DIALING
            Call.STATE_ACTIVE -> TelephonyCallState.ACTIVE
            Call.STATE_HOLDING -> TelephonyCallState.HOLDING
            Call.STATE_DISCONNECTING -> TelephonyCallState.DISCONNECTING
            Call.STATE_DISCONNECTED -> TelephonyCallState.DISCONNECTED
            else -> TelephonyCallState.IDLE
        }

        if (stateEnum != TelephonyCallState.RINGING) {
            inCallService?.applicationContext?.let { ctx ->
                IncomingCallNotificationHelper.dismissNotification(ctx)
            }
        }

        val current = _currentCallInfo.value
        val connectTime = if (stateEnum == TelephonyCallState.ACTIVE && (current?.connectTimeMillis ?: 0L) == 0L) {
            System.currentTimeMillis()
        } else current?.connectTimeMillis ?: 0L

        _currentCallInfo.value = ActiveCallInfo(
            callId = call.toString(),
            number = schemeSpecific,
            displayName = callerName.ifBlank { PhoneNumberHelper.formatForDisplay(schemeSpecific) },
            state = stateEnum,
            connectTimeMillis = connectTime,
            isMuted = current?.isMuted ?: false,
            isSpeakerOn = current?.isSpeakerOn ?: false,
            isBluetoothOn = current?.isBluetoothOn ?: false,
            isOnHold = stateEnum == TelephonyCallState.HOLDING
        )
    }

    fun answer() {
        activeCall?.answer(0)
    }

    fun disconnect() {
        if (activeCall != null) {
            activeCall?.disconnect()
        } else {
            _currentCallInfo.value = null
        }
    }

    fun setMuted(mute: Boolean) {
        inCallService?.setMuted(mute)
        _currentCallInfo.value = _currentCallInfo.value?.copy(isMuted = mute)
    }

    fun toggleSpeaker() {
        val current = _currentCallInfo.value ?: return
        val newRoute = if (current.isSpeakerOn) {
            CallAudioState.ROUTE_EARPIECE
        } else {
            CallAudioState.ROUTE_SPEAKER
        }
        inCallService?.setAudioRoute(newRoute)
    }

    fun toggleHold() {
        val call = activeCall ?: return
        if (call.state == Call.STATE_HOLDING) {
            call.unhold()
        } else if (call.state == Call.STATE_ACTIVE) {
            call.hold()
        }
    }

    fun playDtmfTone(digit: Char) {
        activeCall?.playDtmfTone(digit)
    }

    fun stopDtmfTone() {
        activeCall?.stopDtmfTone()
    }

    fun startVideoCall(context: Context, number: String, name: String = "", photoUri: String? = null) {
        try {
            com.example.ui.call.VideoCallActivity.start(context, number, name, photoUri)
        } catch (_: Exception) {}
    }
}
