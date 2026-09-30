package com.example.telephony

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallAudioState
import com.example.SalimApplication
import com.example.data.model.ActiveCallInfo
import com.example.data.model.ContactItem
import com.example.data.model.TelephonyCallState
import com.example.domain.usecase.PhoneNumberHelper
import com.example.ui.call.CallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

object CallManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var inCallService: SalimInCallService? = null
    private var activeCall: Call? = null

    private val _currentCallInfo = MutableStateFlow<ActiveCallInfo?>(null)
    val currentCallInfo: StateFlow<ActiveCallInfo?> = _currentCallInfo.asStateFlow()

    init {
        scope.launch {
            CallRecorder.isRecording.collect { rec ->
                _currentCallInfo.value = _currentCallInfo.value?.copy(isRecording = rec)
            }
        }
        scope.launch {
            CallRecorder.durationSeconds.collect { dur ->
                _currentCallInfo.value = _currentCallInfo.value?.copy(recordingDurationSeconds = dur)
            }
        }
    }

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

    fun getInCallService(): SalimInCallService? = inCallService

    fun initiateOutgoingCall(context: Context, number: String, name: String = "") {
        val clean = PhoneNumberHelper.normalizeNumber(number)
        var contact: ContactItem? = null
        try {
            contact = SalimApplication.instance.contactsRepository.findContactByNumberDirect(clean)
        } catch (_: Exception) {}

        val isSaved = contact != null && contact.name.isNotBlank()
        val displayName = if (isSaved) contact!!.name else name.ifBlank { PhoneNumberHelper.formatForDisplay(clean) }
        val photoUri = contact?.photoUri

        _currentCallInfo.value = ActiveCallInfo(
            callId = System.currentTimeMillis().toString(),
            number = clean,
            displayName = displayName,
            photoUri = photoUri,
            isSavedContact = isSaved,
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

        // If app is opened or if call is outgoing, launch CallActivity immediately.
        // If app is closed or in background and call is incoming, the native heads-up notification
        // appears without taking over full screen over apps like YouTube or Chrome.
        // If device is locked, the fullScreenIntent wakes screen with CallActivity.
        if (call.state != Call.STATE_RINGING || SalimApplication.instance.isAppInForeground) {
            val intent = Intent(context, CallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }
    }

    fun onCallRemoved(call: Call) {
        call.unregisterCallback(callCallback)
        IncomingCallNotificationHelper.stopRingtone()
        inCallService?.applicationContext?.let { ctx ->
            IncomingCallNotificationHelper.dismissNotification(ctx)
        }
        if (CallRecorder.isRecording.value) {
            inCallService?.applicationContext?.let { ctx ->
                CallRecordingService.stop(ctx)
            } ?: CallRecorder.stopRecording()
        }

        if (activeCall == call) {
            _currentCallInfo.value = _currentCallInfo.value?.copy(
                state = TelephonyCallState.DISCONNECTED,
                isRecording = false
            )
            activeCall = null
            Handler(Looper.getMainLooper()).postDelayed({
                _currentCallInfo.value = null
            }, 1200)
        }
    }

    fun onAudioStateChanged(audioState: CallAudioState) {
        val current = _currentCallInfo.value ?: return
        val isMuted = audioState.isMuted
        val isSpeaker = (audioState.route and CallAudioState.ROUTE_SPEAKER) != 0
        val isBluetooth = (audioState.route and CallAudioState.ROUTE_BLUETOOTH) != 0
        val updated = current.copy(
            isMuted = isMuted,
            isSpeakerOn = isSpeaker,
            isBluetoothOn = isBluetooth
        )
        _currentCallInfo.value = updated

        if (updated.state == TelephonyCallState.ACTIVE) {
            inCallService?.applicationContext?.let { ctx ->
                IncomingCallNotificationHelper.showOngoingCallNotification(ctx, updated)
            }
        }
    }

    private fun updateCallState(call: Call?) {
        if (call == null) {
            _currentCallInfo.value = null
            return
        }

        val handle = call.details?.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""
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

        // Fast contact lookup
        var contact: ContactItem? = null
        if (rawNumber.isNotBlank()) {
            try {
                contact = SalimApplication.instance.contactsRepository.findContactByNumberDirect(rawNumber)
            } catch (_: Exception) {}
        }

        val isSaved = contact != null && contact.name.isNotBlank()
        val displayName = if (isSaved) {
            contact!!.name
        } else if (callerName.isNotBlank()) {
            callerName
        } else {
            PhoneNumberHelper.formatForDisplay(rawNumber).ifBlank { "Unknown Caller" }
        }
        val photoUri = contact?.photoUri

        val current = _currentCallInfo.value
        val connectTime = if (stateEnum == TelephonyCallState.ACTIVE && (current?.connectTimeMillis ?: 0L) == 0L) {
            System.currentTimeMillis()
        } else current?.connectTimeMillis ?: 0L

        val isRec = CallRecorder.isRecording.value
        val recDur = CallRecorder.durationSeconds.value

        val newInfo = ActiveCallInfo(
            callId = call.toString(),
            number = rawNumber,
            displayName = displayName,
            photoUri = photoUri,
            isSavedContact = isSaved,
            state = stateEnum,
            connectTimeMillis = connectTime,
            isMuted = current?.isMuted ?: false,
            isSpeakerOn = current?.isSpeakerOn ?: false,
            isBluetoothOn = current?.isBluetoothOn ?: false,
            isOnHold = stateEnum == TelephonyCallState.HOLDING,
            isRecording = isRec,
            recordingDurationSeconds = recDur
        )
        _currentCallInfo.value = newInfo

        // Automatic Call Recording check for standard dialers (Oppo / Vivo / Google Phone)
        if (stateEnum == TelephonyCallState.ACTIVE && !isRec) {
            scope.launch(Dispatchers.IO) {
                try {
                    val settings = SalimApplication.instance.preferencesManager.settingsFlow.firstOrNull()
                    if (settings != null) {
                        val shouldRecord = settings.autoRecordCalls || (settings.autoRecordUnknown && !isSaved)
                        if (shouldRecord) {
                            CallRecordingService.start(
                                context = SalimApplication.instance,
                                number = rawNumber,
                                name = displayName,
                                callId = call.toString()
                            )
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        val context = inCallService?.applicationContext ?: SalimApplication.instance

        when (stateEnum) {
            TelephonyCallState.RINGING -> {
                // Defense-in-depth: check if number is blocked or unknown blocking is active
                if (rawNumber.isNotBlank()) {
                    var isBlocked = false
                    var blockUnknown = false
                    try {
                        kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                            val app = SalimApplication.instance
                            isBlocked = app.blockedRepository.isNumberBlocked(rawNumber)
                            blockUnknown = app.preferencesManager.settingsFlow.firstOrNull()?.blockUnknownNumbers == true
                        }
                    } catch (_: Exception) {}

                    if (isBlocked || (blockUnknown && contact == null)) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                call.reject(Call.REJECT_REASON_DECLINED)
                            } else {
                                @Suppress("DEPRECATION")
                                call.reject(false, null)
                            }
                        } catch (_: Exception) {
                            call.disconnect()
                        }
                        return
                    }
                }
                IncomingCallNotificationHelper.showIncomingCallNotification(context, call, contact)
            }
            TelephonyCallState.ACTIVE -> {
                IncomingCallNotificationHelper.stopRingtone()
                IncomingCallNotificationHelper.showOngoingCallNotification(context, newInfo)
            }
            TelephonyCallState.DISCONNECTING, TelephonyCallState.DISCONNECTED -> {
                IncomingCallNotificationHelper.stopRingtone()
                IncomingCallNotificationHelper.dismissNotification(context)
                if (CallRecorder.isRecording.value) {
                    CallRecordingService.stop(context)
                }
            }
            else -> {
                IncomingCallNotificationHelper.stopRingtone()
            }
        }
    }

    fun answer() {
        IncomingCallNotificationHelper.stopRingtone()
        activeCall?.answer(0)
    }

    fun disconnect() {
        IncomingCallNotificationHelper.stopRingtone()
        inCallService?.applicationContext?.let { ctx ->
            IncomingCallNotificationHelper.dismissNotification(ctx)
        }
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

        // Update local state directly for responsive feedback
        val updated = current.copy(isSpeakerOn = !current.isSpeakerOn)
        _currentCallInfo.value = updated

        if (updated.state == TelephonyCallState.ACTIVE) {
            inCallService?.applicationContext?.let { ctx ->
                IncomingCallNotificationHelper.showOngoingCallNotification(ctx, updated)
            }
        }
    }

    fun toggleHold() {
        val call = activeCall ?: return
        if (call.state == Call.STATE_HOLDING) {
            call.unhold()
        } else if (call.state == Call.STATE_ACTIVE) {
            call.hold()
        }
    }

    private var inCallToneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_VOICE_CALL, 80)
    } catch (_: Exception) {
        null
    }

    fun playDtmfTone(digit: Char) {
        activeCall?.playDtmfTone(digit)
        val tone = when (digit) {
            '0' -> ToneGenerator.TONE_DTMF_0
            '1' -> ToneGenerator.TONE_DTMF_1
            '2' -> ToneGenerator.TONE_DTMF_2
            '3' -> ToneGenerator.TONE_DTMF_3
            '4' -> ToneGenerator.TONE_DTMF_4
            '5' -> ToneGenerator.TONE_DTMF_5
            '6' -> ToneGenerator.TONE_DTMF_6
            '7' -> ToneGenerator.TONE_DTMF_7
            '8' -> ToneGenerator.TONE_DTMF_8
            '9' -> ToneGenerator.TONE_DTMF_9
            '*' -> ToneGenerator.TONE_DTMF_S
            '#' -> ToneGenerator.TONE_DTMF_P
            else -> -1
        }
        if (tone != -1) {
            try {
                inCallToneGenerator?.startTone(tone, 120)
            } catch (_: Exception) {}
        }
    }

    fun stopDtmfTone() {
        activeCall?.stopDtmfTone()
        try {
            inCallToneGenerator?.stopTone()
        } catch (_: Exception) {}
    }

    fun startVideoCall(context: Context, number: String, name: String = "", photoUri: String? = null) {
        try {
            com.example.ui.call.VideoCallActivity.start(context, number, name, photoUri)
        } catch (_: Exception) {}
    }

    fun toggleRecording(context: Context) {
        val current = _currentCallInfo.value ?: return
        if (CallRecorder.isRecording.value) {
            CallRecordingService.stop(context)
        } else {
            CallRecordingService.start(
                context = context,
                number = current.number,
                name = current.displayName,
                callId = current.callId
            )
        }
    }
}
