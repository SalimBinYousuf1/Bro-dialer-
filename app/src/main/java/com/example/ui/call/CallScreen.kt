package com.example.ui.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveCallInfo
import com.example.data.model.TelephonyCallState
import com.example.domain.usecase.PhoneNumberHelper
import com.example.ui.components.SalimAvatar
import com.example.ui.dialpad.DialpadKey
import com.example.ui.dialpad.KeypadButtonDef
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimRed
import com.example.ui.theme.SalimWhite
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    callInfo: ActiveCallInfo?,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onMuteToggle: (Boolean) -> Unit,
    onSpeakerToggle: () -> Unit,
    onHoldToggle: () -> Unit,
    onDtmfTone: (Char) -> Unit,
    onDtmfStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var showInCallKeypad by remember { mutableStateOf(false) }

    val state = callInfo?.state ?: TelephonyCallState.IDLE

    LaunchedEffect(state, callInfo?.connectTimeMillis) {
        if (state == TelephonyCallState.ACTIVE) {
            val startTime = callInfo?.connectTimeMillis?.takeIf { it > 0 } ?: System.currentTimeMillis()
            while (true) {
                elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                delay(1000)
            }
        }
    }

    val statusLabel = when (state) {
        TelephonyCallState.RINGING -> "Incoming Call"
        TelephonyCallState.DIALING -> "Calling..."
        TelephonyCallState.ACTIVE -> {
            val mins = elapsedSeconds / 60
            val secs = elapsedSeconds % 60
            String.format("%02d:%02d", mins, secs)
        }
        TelephonyCallState.HOLDING -> "Call on Hold"
        TelephonyCallState.DISCONNECTING -> "Disconnecting..."
        TelephonyCallState.DISCONNECTED -> "Call Ended"
        TelephonyCallState.IDLE -> ""
    }

    Surface(
        color = Color(0xFF1C1C1E), // Dark aesthetic for calling experience
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Caller Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                SalimAvatar(
                    name = callInfo?.displayName ?: "Unknown",
                    size = 92.dp
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = callInfo?.displayName ?: "Unknown",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (!callInfo?.number.isNullOrBlank()) {
                        PhoneNumberHelper.formatForDisplay(callInfo?.number ?: "")
                    } else "Unknown Number",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (state == TelephonyCallState.RINGING) SalimGreen else Color.White.copy(alpha = 0.85f)
                )
            }

            // Middle Section: Controls Grid or In-Call Keypad
            if (state == TelephonyCallState.ACTIVE || state == TelephonyCallState.HOLDING || state == TelephonyCallState.DIALING) {
                if (showInCallKeypad) {
                    InCallKeypadOverlay(
                        onDigitPress = onDtmfTone,
                        onDigitRelease = onDtmfStop,
                        onClose = { showInCallKeypad = false }
                    )
                } else {
                    InCallControlsGrid(
                        isMuted = callInfo?.isMuted ?: false,
                        isSpeakerOn = callInfo?.isSpeakerOn ?: false,
                        isOnHold = callInfo?.isOnHold ?: false,
                        onMuteToggle = { onMuteToggle(callInfo?.isMuted ?: false) },
                        onSpeakerToggle = onSpeakerToggle,
                        onHoldToggle = onHoldToggle,
                        onKeypadToggle = { showInCallKeypad = true }
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Bottom Actions (Incoming vs Active)
            if (state == TelephonyCallState.RINGING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decline Button
                    CallActionButton(
                        icon = Icons.Default.CallEnd,
                        label = "Decline",
                        backgroundColor = SalimRed,
                        iconTint = SalimWhite,
                        onClick = onDecline,
                        testTag = "call_decline_button"
                    )

                    // Answer Button
                    CallActionButton(
                        icon = Icons.Default.Call,
                        label = "Accept",
                        backgroundColor = SalimGreen,
                        iconTint = SalimWhite,
                        onClick = onAnswer,
                        testTag = "call_answer_button"
                    )
                }
            } else {
                // End Call Button
                Box(
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(SalimRed)
                        .clickable(onClick = onDecline)
                        .testTag("call_end_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = SalimWhite,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InCallControlsGrid(
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isOnHold: Boolean,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onHoldToggle: () -> Unit,
    onKeypadToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InCallIconButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (isMuted) "Unmute" else "Mute",
                isActive = isMuted,
                onClick = onMuteToggle
            )
            InCallIconButton(
                icon = Icons.Default.Dialpad,
                label = "Keypad",
                isActive = false,
                onClick = onKeypadToggle
            )
            InCallIconButton(
                icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                label = "Speaker",
                isActive = isSpeakerOn,
                onClick = onSpeakerToggle
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InCallIconButton(
                icon = Icons.Default.Pause,
                label = if (isOnHold) "Unhold" else "Hold",
                isActive = isOnHold,
                onClick = onHoldToggle
            )
            InCallIconButton(
                icon = Icons.Default.Person,
                label = "Contacts",
                isActive = false,
                onClick = {}
            )
        }
    }
}

@Composable
private fun InCallIconButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color(0xFF2C2C2E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}

@Composable
private fun InCallKeypadOverlay(
    onDigitPress: (Char) -> Unit,
    onDigitRelease: () -> Unit,
    onClose: () -> Unit
) {
    val keys = listOf(
        listOf(KeypadButtonDef('1', "", "incall_key_1"), KeypadButtonDef('2', "ABC", "incall_key_2"), KeypadButtonDef('3', "DEF", "incall_key_3")),
        listOf(KeypadButtonDef('4', "GHI", "incall_key_4"), KeypadButtonDef('5', "JKL", "incall_key_5"), KeypadButtonDef('6', "MNO", "incall_key_6")),
        listOf(KeypadButtonDef('7', "PQRS", "incall_key_7"), KeypadButtonDef('8', "TUV", "incall_key_8"), KeypadButtonDef('9', "WXYZ", "incall_key_9")),
        listOf(KeypadButtonDef('*', "", "incall_key_star"), KeypadButtonDef('0', "+", "incall_key_0"), KeypadButtonDef('#', "", "incall_key_hash"))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { def ->
                    DialpadKey(
                        def = def,
                        onClick = { onDigitPress(def.digit) },
                        onLongClick = {}
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hide Keypad",
            color = SalimBlue,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .clickable(onClick = onClose)
                .padding(8.dp)
        )
    }
}
