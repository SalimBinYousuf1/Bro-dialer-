package com.example.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ui.call.CallActivity

class CallNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            ACTION_ANSWER -> {
                IncomingCallNotificationHelper.dismissNotification(context)
                CallManager.answer()

                // Launch CallActivity to show active call screen
                val callIntent = Intent(context, CallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(callIntent)
            }
            ACTION_DECLINE -> {
                IncomingCallNotificationHelper.dismissNotification(context)
                CallManager.disconnect()
            }
        }
    }

    companion object {
        const val ACTION_ANSWER = "com.example.telephony.ACTION_ANSWER_CALL"
        const val ACTION_DECLINE = "com.example.telephony.ACTION_DECLINE_CALL"
    }
}
