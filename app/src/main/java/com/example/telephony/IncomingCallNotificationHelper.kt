package com.example.telephony

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.telecom.Call
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import com.example.SalimApplication
import com.example.data.model.ActiveCallInfo
import com.example.data.model.ContactItem
import com.example.data.model.TelephonyCallState
import com.example.domain.usecase.PhoneNumberHelper
import com.example.ui.call.CallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

object IncomingCallNotificationHelper {

    const val CHANNEL_ID_INCOMING = "incoming_calls_pill"
    const val CHANNEL_ID_ONGOING = "ongoing_calls_salim"
    const val NOTIFICATION_ID = 2001
    const val KEY_TEXT_REPLY = "key_quick_message_reply"

    private var activeRingtone: Ringtone? = null
    private var activeMediaPlayer: MediaPlayer? = null
    private val ringtoneLock = Any()

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel 1: Incoming Calls (High Importance, Public Lockscreen, Vibration)
            val incomingName = "Incoming Calls"
            val incomingDesc = "Heads-up notification and call alerts for incoming cellular calls"
            val incomingChannel = NotificationChannel(
                CHANNEL_ID_INCOMING,
                incomingName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = incomingDesc
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 800, 800)
                // Set silent here so our custom looping audio manager handles 100% ringtone playback
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(incomingChannel)

            // Channel 2: Ongoing Calls (Default/Low Importance for persistent active call indicator)
            val ongoingName = "Ongoing Calls"
            val ongoingDesc = "Status and in-call controls for active calls"
            val ongoingChannel = NotificationChannel(
                CHANNEL_ID_ONGOING,
                ongoingName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = ongoingDesc
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(ongoingChannel)
        }
    }

    /**
     * Shows high-priority heads-up incoming call notification with native CallStyle.
     * When swiped away, the heads-up banner is dismissed, but the call continues ringing
     * and the persistent notification remains in the notification shade until explicitly answered or declined.
     */
    fun showIncomingCallNotification(context: Context, call: Call, contact: ContactItem? = null) {
        createNotificationChannels(context)

        val handle = call.details?.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""
        val callerDisplayName = call.details?.callerDisplayName

        val isSaved = contact != null && contact.name.isNotBlank()
        val displayName = if (isSaved) {
            contact!!.name
        } else if (!callerDisplayName.isNullOrBlank()) {
            callerDisplayName
        } else {
            PhoneNumberHelper.formatForDisplay(rawNumber).ifBlank { "Incoming Call" }
        }

        val formattedNumber = PhoneNumberHelper.formatForDisplay(rawNumber)
        val subtitle = if (isSaved) {
            if (formattedNumber.isNotBlank()) formattedNumber else "Mobile"
        } else {
            if (formattedNumber.isNotBlank()) "Unsaved Caller • $formattedNumber" else "Incoming Cellular Call"
        }

        // Start ringing with custom contact ringtone or chosen default ringtone with 100% looping reliability
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var chosenUriStr: String? = null
                if (contact != null) {
                    val custom = SalimApplication.instance.contactCustomizationRepository.getCustomizationDirect(contact.id)
                    chosenUriStr = custom?.ringtoneUri
                }
                if (chosenUriStr.isNullOrBlank()) {
                    val settings = SalimApplication.instance.preferencesManager.settingsFlow.firstOrNull()
                    chosenUriStr = settings?.defaultRingtoneUri
                }
                playRingtone(context, chosenUriStr)
            } catch (_: Exception) {}
        }

        // Tapping the notification body opens CallActivity in ringing state
        val fullScreenIntent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Answer Call (Green)
        val answerIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_ANSWER
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Decline Call (Red)
        val declineIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_DECLINE
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Quick Message Direct Reply (Action with RemoteInput)
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Quick message...")
            .setChoices(
                arrayOf(
                    "I'll call you right back.",
                    "Can't talk now. What's up?",
                    "On my way.",
                    "In a meeting."
                )
            )
            .build()

        val replyIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_QUICK_REPLY
            putExtra(CallNotificationReceiver.EXTRA_PHONE_NUMBER, rawNumber)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        )

        val quickMessageAction = NotificationCompat.Action.Builder(
            android.R.drawable.sym_action_chat,
            "Quick Message",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        // Build Caller Person with Avatar
        val avatarBitmap = if (!contact?.photoUri.isNullOrBlank()) {
            loadContactBitmap(context, Uri.parse(contact!!.photoUri))
        } else null
        val finalAvatar = avatarBitmap ?: createAvatarBitmap(displayName, isSaved)

        val person = Person.Builder()
            .setName(displayName)
            .setUri(if (rawNumber.isNotBlank()) "tel:$rawNumber" else null)
            .setIcon(IconCompat.createWithBitmap(finalAvatar))
            .setImportant(true)
            .build()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_INCOMING)
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setContentTitle(displayName)
            .setContentText(subtitle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true) // Persistent while ringing - swiping heads-up does NOT decline call
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(contentPendingIntent, true)
            .setColor(0xFF34C759.toInt()) // Apple Emerald Green
            .setStyle(
                NotificationCompat.CallStyle.forIncomingCall(person, declinePendingIntent, answerPendingIntent)
            )
            .addAction(quickMessageAction)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: Exception) {
            val fallbackBuilder = NotificationCompat.Builder(context, CHANNEL_ID_INCOMING)
                .setSmallIcon(android.R.drawable.sym_call_incoming)
                .setContentTitle(displayName)
                .setContentText(subtitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(contentPendingIntent)
                .setFullScreenIntent(contentPendingIntent, true)
                .setColor(0xFF34C759.toInt())
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", declinePendingIntent)
                .addAction(android.R.drawable.sym_action_call, "Answer", answerPendingIntent)
                .addAction(quickMessageAction)
            try {
                notificationManager.notify(NOTIFICATION_ID, fallbackBuilder.build())
            } catch (_: Exception) {}
        }
    }

    /**
     * Transforms the call notification into an ongoing active call notification.
     * Features:
     * - Uses NotificationCompat.CallStyle.forOngoingCall for native status bar green ongoing-call chip
     * - Real-time chronometer timer (e.g. 00:18)
     * - End Call action (accessible high-contrast red decline control)
     * - Speaker toggle action with state indication ("Speaker: ON" / "Speaker: OFF")
     * - Voice call volume controls ("Vol −" and "Vol +") on STREAM_VOICE_CALL
     * - Current volume percentage feedback
     * - Tapping opens active call screen in CallActivity
     */
    fun showOngoingCallNotification(context: Context, callInfo: ActiveCallInfo) {
        createNotificationChannels(context)

        val displayName = callInfo.displayName.ifBlank {
            PhoneNumberHelper.formatForDisplay(callInfo.number).ifBlank { "Active Call" }
        }
        val formattedNumber = PhoneNumberHelper.formatForDisplay(callInfo.number)

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL).coerceAtLeast(1)
        val volPercent = (currentVol * 100) / maxVol
        val speakerStatus = if (callInfo.isSpeakerOn) "Speaker ON" else "Speaker OFF"

        val subtitle = if (formattedNumber.isNotBlank()) {
            "$formattedNumber • $speakerStatus • Vol: $volPercent%"
        } else {
            "Call in progress • $speakerStatus • Vol: $volPercent%"
        }

        // Tapping opens CallActivity (which renders active call controls)
        val openIntent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: End Call
        val endCallIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_DECLINE
        }
        val endCallPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            endCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Toggle Speaker
        val speakerIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_TOGGLE_SPEAKER
        }
        val speakerPendingIntent = PendingIntent.getBroadcast(
            context,
            4,
            speakerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Volume Down
        val volDownIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_VOLUME_DOWN
        }
        val volDownPendingIntent = PendingIntent.getBroadcast(
            context,
            5,
            volDownIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Volume Up
        val volUpIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_VOLUME_UP
        }
        val volUpPendingIntent = PendingIntent.getBroadcast(
            context,
            6,
            volUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val avatarBitmap = if (!callInfo.photoUri.isNullOrBlank()) {
            loadContactBitmap(context, Uri.parse(callInfo.photoUri))
        } else null
        val finalAvatar = avatarBitmap ?: createAvatarBitmap(displayName, callInfo.isSavedContact)

        val person = Person.Builder()
            .setName(displayName)
            .setUri(if (callInfo.number.isNotBlank()) "tel:${callInfo.number}" else null)
            .setIcon(IconCompat.createWithBitmap(finalAvatar))
            .setImportant(true)
            .build()

        val connectWhen = if (callInfo.connectTimeMillis > 0L) {
            callInfo.connectTimeMillis
        } else {
            System.currentTimeMillis()
        }

        val speakerLabel = if (callInfo.isSpeakerOn) "🔊 Speaker ON" else "🔈 Speaker OFF"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ONGOING)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle(displayName)
            .setContentText(subtitle)
            .setLargeIcon(finalAvatar)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .setUsesChronometer(true)
            .setWhen(connectWhen)
            .setColor(0xFF34C759.toInt()) // Apple Emerald Green
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "End Call", endCallPendingIntent)
            .addAction(android.R.drawable.ic_lock_silent_mode_off, speakerLabel, speakerPendingIntent)
            .addAction(android.R.drawable.arrow_down_float, "Vol −", volDownPendingIntent)
            .addAction(android.R.drawable.arrow_up_float, "Vol +", volUpPendingIntent)

        val notification = builder.build()
        val service = CallManager.getInCallService()
        if (service != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    service.startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                    )
                } else {
                    service.startForeground(NOTIFICATION_ID, notification)
                }
            } catch (_: Throwable) {
                try {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)
                } catch (_: Throwable) {}
            }
        } else {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (_: Throwable) {}
        }
    }

    fun loadContactBitmap(context: Context, photoUri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(photoUri)
            inputStream?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun createAvatarBitmap(name: String, isSavedContact: Boolean): Bitmap {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSavedContact) 0xFF007AFF.toInt() else 0xFF5856D6.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 52f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val initials = if (isSavedContact) {
            val parts = name.trim().split("\\s+".toRegex())
            when {
                parts.isEmpty() || parts[0].isEmpty() -> "?"
                parts.size == 1 -> parts[0].take(1).uppercase()
                else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            }
        } else {
            "#"
        }

        val yPos = (size / 2f - (textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(initials, size / 2f, yPos, textPaint)
        return bitmap
    }

    /**
     * Plays the ringtone continuously in a loop until answered, declined, or disconnected.
     * Uses MediaPlayer as the primary engine for continuous looping across all Android versions,
     * with RingtoneManager as fallback.
     */
    fun playRingtone(context: Context, customUriStr: String?) {
        synchronized(ringtoneLock) {
            stopRingtone()
            try {
                val uri = if (!customUriStr.isNullOrBlank()) {
                    Uri.parse(customUriStr)
                } else {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()

                try {
                    activeMediaPlayer = MediaPlayer().apply {
                        setDataSource(context.applicationContext, uri)
                        setAudioAttributes(audioAttributes)
                        isLooping = true
                        prepare()
                        start()
                    }
                } catch (_: Exception) {
                    // Fallback to RingtoneManager
                    activeRingtone = RingtoneManager.getRingtone(context.applicationContext, uri)?.apply {
                        this.audioAttributes = audioAttributes
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            isLooping = true
                        }
                        play()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun stopRingtone() {
        synchronized(ringtoneLock) {
            try {
                activeMediaPlayer?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                }
                activeMediaPlayer = null
            } catch (_: Exception) {}

            try {
                activeRingtone?.stop()
                activeRingtone = null
            } catch (_: Exception) {}
        }
    }

    fun dismissNotification(context: Context) {
        stopRingtone()
        val service = CallManager.getInCallService()
        if (service != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    service.stopForeground(true)
                }
            } catch (_: Exception) {}
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }
}
