package com.example.ui.call

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.SalimApplication
import com.example.data.model.CallType
import com.example.ui.theme.SalimTheme
import kotlinx.coroutines.launch

class VideoCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

        val number = intent.getStringExtra(EXTRA_NUMBER) ?: ""
        val name = intent.getStringExtra(EXTRA_NAME) ?: ""
        val photoUri = intent.getStringExtra(EXTRA_PHOTO_URI)

        setContent {
            SalimTheme {
                VideoCallScreen(
                    contactName = name,
                    phoneNumber = number,
                    photoUri = photoUri,
                    onEndCall = { duration ->
                        lifecycleScope.launch {
                            // Log the video call
                            try {
                                SalimApplication.instance.callLogRepository.logCall(
                                    number = number,
                                    name = name.ifBlank { null },
                                    type = CallType.OUTGOING,
                                    durationSeconds = duration
                                )
                            } catch (_: Exception) {}
                            finish()
                        }
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_NUMBER = "extra_video_number"
        const val EXTRA_NAME = "extra_video_name"
        const val EXTRA_PHOTO_URI = "extra_video_photo"

        fun start(context: Context, number: String, name: String = "", photoUri: String? = null) {
            val intent = Intent(context, VideoCallActivity::class.java).apply {
                putExtra(EXTRA_NUMBER, number)
                putExtra(EXTRA_NAME, name)
                putExtra(EXTRA_PHOTO_URI, photoUri)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }
    }
}
