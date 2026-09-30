package com.example.telephony

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.SalimApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallbackReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val scope = CoroutineScope(Dispatchers.IO)

        when (action) {
            ACTION_TRIGGER_REMINDER -> {
                val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
                val number = intent.getStringExtra(EXTRA_NUMBER) ?: ""
                val name = intent.getStringExtra(EXTRA_NAME) ?: number
                val note = intent.getStringExtra(EXTRA_NOTE) ?: ""

                showReminderNotification(context, reminderId, number, name, note)
            }

            ACTION_CALL_NOW -> {
                val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
                val number = intent.getStringExtra(EXTRA_NUMBER) ?: ""

                dismissNotification(context, reminderId)

                if (reminderId > 0) {
                    scope.launch {
                        SalimApplication.instance.callbackReminderRepository.markCompleted(reminderId)
                    }
                }

                if (number.isNotBlank()) {
                    try {
                        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(callIntent)
                    } catch (_: Exception) {
                        try {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(dialIntent)
                        } catch (_: Exception) {}
                    }
                }
            }

            ACTION_DISMISS_REMINDER -> {
                val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
                dismissNotification(context, reminderId)
                if (reminderId > 0) {
                    scope.launch {
                        SalimApplication.instance.callbackReminderRepository.markCompleted(reminderId)
                    }
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // Reschedule all upcoming pending reminders after reboot
                scope.launch {
                    try {
                        val reminders = SalimApplication.instance.callbackReminderRepository.getUpcomingPending()
                        reminders.forEach { reminder ->
                            CallbackReminderManager.schedule(
                                context = context,
                                reminderId = reminder.id,
                                number = reminder.phoneNumber,
                                name = reminder.contactName,
                                triggerAtMillis = reminder.remindTimeMillis,
                                note = reminder.note
                            )
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun showReminderNotification(
        context: Context,
        reminderId: Long,
        number: String,
        name: String,
        note: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Callback Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to call someone back"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Call Now Action
        val callIntent = Intent(context, CallbackReminderReceiver::class.java).apply {
            action = ACTION_CALL_NOW
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_NUMBER, number)
        }
        val callPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 1).toInt(),
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Dismiss Action
        val dismissIntent = Intent(context, CallbackReminderReceiver::class.java).apply {
            action = ACTION_DISMISS_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 2).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val contentText = if (note.isNotBlank()) "$note ($number)" else "Time to call back $number"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_call)
            .setContentTitle("Callback Reminder: $name")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(callPendingIntent)
            .addAction(android.R.drawable.sym_action_call, "Call Now", callPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .build()

        notificationManager.notify((NOTIFICATION_BASE_ID + reminderId).toInt(), notification)
    }

    private fun dismissNotification(context: Context, reminderId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel((NOTIFICATION_BASE_ID + reminderId).toInt())
    }

    companion object {
        const val CHANNEL_ID = "channel_callback_reminders"
        const val NOTIFICATION_BASE_ID = 50000

        const val ACTION_TRIGGER_REMINDER = "com.example.telephony.action.TRIGGER_REMINDER"
        const val ACTION_CALL_NOW = "com.example.telephony.action.CALL_NOW"
        const val ACTION_DISMISS_REMINDER = "com.example.telephony.action.DISMISS_REMINDER"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_NUMBER = "extra_number"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_NOTE = "extra_note"
    }
}
