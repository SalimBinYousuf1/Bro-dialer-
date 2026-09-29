package com.example.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.core.app.RemoteInput
import com.example.SalimApplication
import com.example.data.model.TelephonyCallState

class CallNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            ACTION_ANSWER -> {
                IncomingCallNotificationHelper.stopRingtone()
                CallManager.answer()

                // Immediately transition notification to ongoing active call state
                val currentInfo = CallManager.currentCallInfo.value
                val activeInfo = currentInfo?.copy(
                    state = TelephonyCallState.ACTIVE,
                    connectTimeMillis = System.currentTimeMillis()
                ) ?: com.example.data.model.ActiveCallInfo(
                    state = TelephonyCallState.ACTIVE,
                    connectTimeMillis = System.currentTimeMillis()
                )
                IncomingCallNotificationHelper.showOngoingCallNotification(context, activeInfo)
            }

            ACTION_DECLINE -> {
                IncomingCallNotificationHelper.dismissNotification(context)
                CallManager.disconnect()
            }

            ACTION_TOGGLE_SPEAKER -> {
                CallManager.toggleSpeaker()
                CallManager.currentCallInfo.value?.let { info ->
                    IncomingCallNotificationHelper.showOngoingCallNotification(context, info)
                }
            }

            ACTION_VOLUME_UP -> {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI
                )
                CallManager.currentCallInfo.value?.let { info ->
                    IncomingCallNotificationHelper.showOngoingCallNotification(context, info)
                }
            }

            ACTION_VOLUME_DOWN -> {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI
                )
                CallManager.currentCallInfo.value?.let { info ->
                    IncomingCallNotificationHelper.showOngoingCallNotification(context, info)
                }
            }

            ACTION_QUICK_REPLY -> {
                val remoteInput = RemoteInput.getResultsFromIntent(intent)
                val replyText = remoteInput?.getCharSequence(IncomingCallNotificationHelper.KEY_TEXT_REPLY)?.toString()
                    ?: intent.getStringExtra(EXTRA_MESSAGE_TEXT)
                val rawNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
                    ?: CallManager.currentCallInfo.value?.number ?: ""

                if (!replyText.isNullOrBlank() && rawNumber.isNotBlank()) {
                    SalimApplication.instance.telecomRepository.sendDirectSms(rawNumber, replyText)
                }
                CallManager.disconnect()
                IncomingCallNotificationHelper.dismissNotification(context)
            }
        }
    }

    companion object {
        const val ACTION_ANSWER = "com.example.telephony.ACTION_ANSWER_CALL"
        const val ACTION_DECLINE = "com.example.telephony.ACTION_DECLINE_CALL"
        const val ACTION_TOGGLE_SPEAKER = "com.example.telephony.ACTION_TOGGLE_SPEAKER"
        const val ACTION_VOLUME_UP = "com.example.telephony.ACTION_VOLUME_UP"
        const val ACTION_VOLUME_DOWN = "com.example.telephony.ACTION_VOLUME_DOWN"
        const val ACTION_QUICK_REPLY = "com.example.telephony.ACTION_QUICK_REPLY"

        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_MESSAGE_TEXT = "extra_message_text"
    }
}
