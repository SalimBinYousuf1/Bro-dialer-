package com.example.ui.voicemail

import android.content.Context
import android.telephony.TelephonyManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SalimApplication
import com.example.ui.components.SalimBackButton
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostCard
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass

@Composable
fun VoicemailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val tm = remember { context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager }

    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    val voicemailNumber = remember {
        try {
            tm?.voiceMailNumber?.takeIf { it.isNotBlank() } ?: "*86"
        } catch (_: Exception) {
            "*86"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SalimBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Voicemail",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .size(86.dp)
                    .liquidGlass(shape = CircleShape, elevation = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Voicemail,
                    contentDescription = null,
                    tint = textPrimary,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Carrier Voicemail",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Access your carrier voicemail box directly with one tap or by long-pressing 1 on the dialpad.",
                style = MaterialTheme.typography.bodyMedium,
                color = textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            FrostCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Configured Access Number",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = voicemailNumber,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FrostButton(
                text = "Call Voicemail ($voicemailNumber)",
                icon = Icons.Default.Call,
                onClick = {
                    SalimApplication.instance.telecomRepository.makeCall(voicemailNumber)
                },
                isProminent = true,
                modifier = Modifier.fillMaxWidth(0.9f),
                testTag = "call_voicemail_button"
            )
        }
    }
}
