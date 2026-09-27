package com.example.telephony

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.telecom.Call
import androidx.core.app.NotificationCompat
import com.example.SalimApplication
import com.example.domain.usecase.PhoneNumberHelper
import com.example.ui.call.CallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

object IncomingCallNotificationHelper {

    const val CHANNEL_ID = "incoming_calls"
    const val NOTIFICATION_ID = 2001
    private var activeRingtone: Ringtone? = null

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Incoming Calls"
            val descriptionText = "Heads-up notifications for incoming phone calls"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 800, 800)
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
                setSound(ringtoneUri, audioAttributes)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showIncomingCallNotification(context: Context, call: Call) {
        createNotificationChannel(context)

        val handle = call.details?.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""
        val callerDisplayName = call.details?.callerDisplayName
        val displayName = callerDisplayName?.takeIf { it.isNotBlank() }
            ?: PhoneNumberHelper.formatForDisplay(rawNumber).ifBlank { "Incoming Call" }

        val subtitle = if (callerDisplayName != null && callerDisplayName.isNotBlank() && rawNumber.isNotBlank()) {
            PhoneNumberHelper.formatForDisplay(rawNumber)
        } else {
            "Incoming Phone Call"
        }

        // Start ringing with custom ringtone if available
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var chosenUriStr: String? = null
                if (rawNumber.isNotBlank()) {
                    val contact = PhoneNumberHelper.findContactForNumber(
                        SalimApplication.instance.contactsRepository.loadContacts(),
                        rawNumber
                    )
                    if (contact != null) {
                        val custom = SalimApplication.instance.contactCustomizationRepository.getCustomizationDirect(contact.id)
                        chosenUriStr = custom?.ringtoneUri
                    }
                }
                if (chosenUriStr.isNullOrBlank()) {
                    val settings = SalimApplication.instance.preferencesManager.settingsFlow.firstOrNull()
                    chosenUriStr = settings?.defaultRingtoneUri
                }
                playRingtone(context, chosenUriStr)
            } catch (_: Exception) {}
        }

        // Full screen & content intent to CallActivity
        val fullScreenIntent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Answer Call
        val answerIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_ANSWER
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Decline Call
        val declineIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_DECLINE
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setContentTitle(displayName)
            .setContentText(subtitle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", declinePendingIntent)
            .addAction(android.R.drawable.sym_action_call, "Answer", answerPendingIntent)
            .setColor(0xFF24A1DE.toInt()) // Liquid blue accent
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun playRingtone(context: Context, customUriStr: String?) {
        stopRingtone()
        try {
            val uri = if (!customUriStr.isNullOrBlank()) Uri.parse(customUriStr) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            activeRingtone = RingtoneManager.getRingtone(context.applicationContext, uri)?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = true
                }
                play()
            }
        } catch (_: Exception) {}
    }

    fun stopRingtone() {
        try {
            activeRingtone?.stop()
            activeRingtone = null
        } catch (_: Exception) {}
    }

    fun dismissNotification(context: Context) {
        stopRingtone()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
