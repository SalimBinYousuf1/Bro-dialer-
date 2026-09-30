package com.example.ui.call

import android.app.KeyguardManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.SalimApplication
import com.example.data.model.ContactCustomization
import com.example.domain.usecase.PhoneNumberHelper
import com.example.telephony.CallManager
import com.example.telephony.IncomingCallNotificationHelper
import com.example.ui.theme.SalimTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CallActivity : ComponentActivity() {

    private var proximityWakeLock: android.os.PowerManager.WakeLock? = null
    private var isInPipMode by mutableStateOf(false)

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val callInfo = CallManager.currentCallInfo.value
        // Standard Android dialer behavior (Google Phone / Oppo / Vivo):
        // When user leaves during an active call, transition to Picture-in-Picture
        if (callInfo?.state == com.example.data.model.TelephonyCallState.ACTIVE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val params = PictureInPictureParams.Builder()
                        .setAspectRatio(Rational(9, 16))
                        .build()
                    enterPictureInPictureMode(params)
                } catch (_: Exception) {}
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Proximity WakeLock to turn screen off when near the ear
        val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        if (powerManager != null && powerManager.isWakeLockLevelSupported(android.os.PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            proximityWakeLock = powerManager.newWakeLock(
                android.os.PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "salimdialer:proximity_wake_lock"
            )
        }

        // Turn screen on and show over lock screen for incoming/active calls
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        var hasHadActiveCall = false
        lifecycleScope.launch {
            CallManager.currentCallInfo.collectLatest { callInfo ->
                if (callInfo != null) {
                    hasHadActiveCall = true
                    // Configure auto Picture-in-Picture on Android 12+ (S) when call is active
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            val pipParams = PictureInPictureParams.Builder()
                                .setAspectRatio(Rational(9, 16))
                                .setAutoEnterEnabled(callInfo.state == com.example.data.model.TelephonyCallState.ACTIVE)
                                .build()
                            setPictureInPictureParams(pipParams)
                        } catch (_: Exception) {}
                    }

                    // Standard dialer proximity sensor management: screen turns off when held to ear
                    if (callInfo.state == com.example.data.model.TelephonyCallState.ACTIVE && !callInfo.isSpeakerOn && !callInfo.isBluetoothOn) {
                        if (proximityWakeLock?.isHeld == false) {
                            try {
                                proximityWakeLock?.acquire(30 * 60 * 1000L)
                            } catch (_: Exception) {}
                        }
                    } else {
                        if (proximityWakeLock?.isHeld == true) {
                            try {
                                proximityWakeLock?.release()
                            } catch (_: Exception) {}
                        }
                    }
                } else if (hasHadActiveCall) {
                    if (proximityWakeLock?.isHeld == true) {
                        try {
                            proximityWakeLock?.release()
                        } catch (_: Exception) {}
                    }
                    finish()
                }
            }
        }

        setContent {
            SalimTheme {
                val callInfo by CallManager.currentCallInfo.collectAsState()
                val settings by SalimApplication.instance.preferencesManager.settingsFlow.collectAsState(initial = null)

                val contactCustomization by produceState<ContactCustomization?>(initialValue = null, callInfo?.number) {
                    val num = callInfo?.number
                    if (!num.isNullOrBlank()) {
                        val contact = PhoneNumberHelper.findContactForNumber(
                            SalimApplication.instance.contactsRepository.loadContacts(),
                            num
                        )
                        if (contact != null) {
                            value = SalimApplication.instance.contactCustomizationRepository.getCustomizationDirect(contact.id)
                        }
                    }
                }

                val effectiveBackgroundUri = contactCustomization?.callBackgroundUri ?: settings?.callBackgroundUri
                val persistentQuickMessages = settings?.quickMessages ?: SalimApplication.instance.preferencesManager.defaultQuickReplies

                CallScreen(
                    callInfo = callInfo,
                    backgroundUri = effectiveBackgroundUri,
                    quickMessages = persistentQuickMessages,
                    isInPipMode = isInPipMode,
                    onUpdateQuickMessages = { updatedList ->
                        lifecycleScope.launch {
                            SalimApplication.instance.preferencesManager.updateQuickMessages(updatedList)
                        }
                    },
                    onAnswer = {
                        IncomingCallNotificationHelper.stopRingtone()
                        CallManager.answer()
                    },
                    onDecline = {
                        IncomingCallNotificationHelper.stopRingtone()
                        val wasRinging = callInfo?.state == com.example.data.model.TelephonyCallState.RINGING
                        CallManager.disconnect()
                        if (wasRinging) {
                            finish()
                        }
                    },
                    onMuteToggle = { CallManager.setMuted(!it) },
                    onSpeakerToggle = { CallManager.toggleSpeaker() },
                    onHoldToggle = { CallManager.toggleHold() },
                    onVideoCall = {
                        val number = callInfo?.number ?: ""
                        CallManager.startVideoCall(this@CallActivity, number)
                    },
                    onToggleRecord = {
                        CallManager.toggleRecording(this@CallActivity)
                    },
                    onDtmfTone = { digit -> CallManager.playDtmfTone(digit) },
                    onDtmfStop = { CallManager.stopDtmfTone() },
                    onSaveNote = { number, name, note ->
                        lifecycleScope.launch {
                            SalimApplication.instance.callNoteRepository.saveNote(number, name, note)
                            saveToDefaultNotesApp(name ?: number, note)
                        }
                    }
                )
            }
        }
    }

    private fun saveToDefaultNotesApp(title: String, content: String) {
        try {
            val noteIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Call Note: $title")
                putExtra(Intent.EXTRA_TITLE, "Call Note: $title")
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(noteIntent, "Save Note to Notes App").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(chooser)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        if (proximityWakeLock?.isHeld == true) {
            try {
                proximityWakeLock?.release()
            } catch (_: Exception) {}
        }
        proximityWakeLock = null
        IncomingCallNotificationHelper.stopRingtone()
        super.onDestroy()
    }
}
