package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.example.domain.usecase.PhoneNumberHelper

enum class VideoProvider(val displayName: String, val packageName: String) {
    WHATSAPP("WhatsApp Video", "com.whatsapp"),
    WHATSAPP_BUSINESS("WhatsApp Business", "com.whatsapp.w4b"),
    GOOGLE_MEET("Google Meet", "com.google.android.apps.tachyon"),
    TELEGRAM("Telegram Video", "org.telegram.messenger")
}

data class AvailableVideoProvider(
    val provider: VideoProvider,
    val isInstalled: Boolean
)

object VideoLauncher {

    fun getInstalledProviders(context: Context): List<VideoProvider> {
        val pm = context.packageManager
        return VideoProvider.values().filter { provider ->
            try {
                pm.getPackageInfo(provider.packageName, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    fun launchWhatsAppVideo(context: Context, rawNumber: String): Boolean {
        val cleanNumber = PhoneNumberHelper.normalizeNumber(rawNumber).removePrefix("+")
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber")
                `package` = "com.whatsapp"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanNumber")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun launchGoogleMeet(context: Context): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.tachyon")
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                true
            } else {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.tachyon")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(storeIntent)
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun launchTelegram(context: Context, rawNumber: String): Boolean {
        val cleanNumber = PhoneNumberHelper.normalizeNumber(rawNumber).removePrefix("+")
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$cleanNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
