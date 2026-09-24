package com.example.ui.call

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.SalimApplication
import com.example.data.model.TelephonyCallState
import com.example.telephony.CallManager
import com.example.ui.theme.SalimTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                } else if (hasHadActiveCall) {
                    finish()
                }
            }
        }

        setContent {
            SalimTheme {
                val callInfo by CallManager.currentCallInfo.collectAsState()
                val settings by SalimApplication.instance.preferencesManager.settingsFlow.collectAsState(initial = null)

                CallScreen(
                    callInfo = callInfo,
                    backgroundUri = settings?.callBackgroundUri,
                    onAnswer = { CallManager.answer() },
                    onDecline = {
                        CallManager.disconnect()
                        finish()
                    },
                    onMuteToggle = { CallManager.setMuted(!it) },
                    onSpeakerToggle = { CallManager.toggleSpeaker() },
                    onHoldToggle = { CallManager.toggleHold() },
                    onVideoCall = {
                        val number = callInfo?.number ?: ""
                        CallManager.startVideoCall(this@CallActivity, number)
                    },
                    onDtmfTone = { digit -> CallManager.playDtmfTone(digit) },
                    onDtmfStop = { CallManager.stopDtmfTone() },
                    onSaveNote = { number, name, note ->
                        lifecycleScope.launch {
                            SalimApplication.instance.callNoteRepository.saveNote(number, name, note)
                        }
                    }
                )
            }
        }
    }
}
