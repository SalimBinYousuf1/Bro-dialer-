package com.example.ui.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.theme.CallCrimson
import com.example.ui.theme.CallEmerald
import com.example.ui.theme.FrostButton
import com.example.ui.theme.liquidGlassInteractive
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
import com.example.ui.theme.liquidGlassInteractive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class InCallTab {
    CONTROLS,
    NOTES,
    KEYPAD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    callInfo: ActiveCallInfo?,
    backgroundUri: String? = null,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onMuteToggle: (Boolean) -> Unit,
    onSpeakerToggle: () -> Unit,
    onHoldToggle: () -> Unit,
    onVideoCall: () -> Unit,
    onDtmfTone: (Char) -> Unit,
    onDtmfStop: () -> Unit,
    onSaveNote: (number: String, name: String?, note: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var activeTab by remember { mutableStateOf(InCallTab.CONTROLS) }
    var keypadDigits by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var copyNotice by remember { mutableStateOf<String?>(null) }
    var showQuickReplySheet by remember { mutableStateOf(false) }

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

    Box(modifier = modifier.fillMaxSize()) {
        // Background layer: custom wallpaper from gallery or modern dark glass titanium
        if (!backgroundUri.isNullOrBlank()) {
            AsyncImage(
                model = backgroundUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Translucent dark scrim for high-contrast legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1C1E))
            )
        }

        // Main Call UI Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section: Caller Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                SalimAvatar(
                    name = callInfo?.displayName ?: "Unknown",
                    size = 80.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = callInfo?.displayName ?: "Unknown",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (!callInfo?.number.isNullOrBlank()) {
                        PhoneNumberHelper.formatForDisplay(callInfo?.number ?: "")
                    } else "Unknown Number",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = if (state == TelephonyCallState.RINGING) SalimGreen else Color.White.copy(alpha = 0.85f)
                )

                // Sleek Apple Pill Segmented Tab Control (Controls | Notes | Keypad)
                if (state == TelephonyCallState.ACTIVE || state == TelephonyCallState.HOLDING || state == TelephonyCallState.DIALING) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InCallTabSegment(
                            title = "Controls",
                            icon = Icons.Default.Tune,
                            isSelected = activeTab == InCallTab.CONTROLS,
                            onClick = { activeTab = InCallTab.CONTROLS },
                            testTag = "incall_tab_controls"
                        )
                        InCallTabSegment(
                            title = "Notes",
                            icon = Icons.Default.EditNote,
                            isSelected = activeTab == InCallTab.NOTES,
                            onClick = { activeTab = InCallTab.NOTES },
                            testTag = "incall_tab_notes"
                        )
                        InCallTabSegment(
                            title = "Keypad",
                            icon = Icons.Default.Dialpad,
                            isSelected = activeTab == InCallTab.KEYPAD,
                            onClick = { activeTab = InCallTab.KEYPAD },
                            testTag = "incall_tab_keypad"
                        )
                    }
                }

                // Brief copy toast banner
                AnimatedVisibility(visible = copyNotice != null) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF3A3A3C))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = copyNotice ?: "",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Bring interactive buttons down towards bottom for comfortable one-handed reach
            Spacer(modifier = Modifier.weight(1.5f))

            // Dynamic Content based on Active Tab
            if (state == TelephonyCallState.ACTIVE || state == TelephonyCallState.HOLDING || state == TelephonyCallState.DIALING) {
                when (activeTab) {
                    InCallTab.CONTROLS -> {
                        InCallControlsGrid(
                            isMuted = callInfo?.isMuted ?: false,
                            isSpeakerOn = callInfo?.isSpeakerOn ?: false,
                            isOnHold = callInfo?.isOnHold ?: false,
                            onMuteToggle = { onMuteToggle(callInfo?.isMuted ?: false) },
                            onKeypadToggle = { activeTab = InCallTab.KEYPAD },
                            onSpeakerToggle = onSpeakerToggle,
                            onVideoCall = onVideoCall,
                            onHoldToggle = onHoldToggle,
                            onNotesToggle = { activeTab = InCallTab.NOTES }
                        )
                    }
                    InCallTab.NOTES -> {
                        InCallNotesView(
                            noteText = noteText,
                            onNoteTextChange = { noteText = it },
                            onCopyNote = {
                                if (noteText.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(noteText))
                                    copyNotice = "Note copied"
                                    scope.launch {
                                        delay(2000)
                                        copyNotice = null
                                    }
                                }
                            },
                            onSaveNote = {
                                if (noteText.isNotBlank()) {
                                    onSaveNote(
                                        callInfo?.number ?: "",
                                        callInfo?.displayName,
                                        noteText
                                    )
                                    copyNotice = "Note saved"
                                    scope.launch {
                                        delay(2000)
                                        copyNotice = null
                                    }
                                    activeTab = InCallTab.CONTROLS
                                }
                            },
                            onBackToControls = { activeTab = InCallTab.CONTROLS }
                        )
                    }
                    InCallTab.KEYPAD -> {
                        InCallKeypadOverlay(
                            enteredDigits = keypadDigits,
                            onDigitPress = { digit ->
                                keypadDigits += digit
                                onDtmfTone(digit)
                            },
                            onDigitRelease = onDtmfStop,
                            onBackspace = {
                                if (keypadDigits.isNotEmpty()) {
                                    keypadDigits = keypadDigits.dropLast(1)
                                }
                            },
                            onCopyDigits = {
                                if (keypadDigits.isNotEmpty()) {
                                    clipboardManager.setText(AnnotatedString(keypadDigits))
                                    copyNotice = "Keypad text copied"
                                    scope.launch {
                                        delay(2000)
                                        copyNotice = null
                                    }
                                }
                            },
                            onClose = { activeTab = InCallTab.CONTROLS }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Ringing State: Answer / Decline / Quick Message | Active State: End Call
            if (state == TelephonyCallState.RINGING) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quick reply canned SMS decline button
                    FrostButton(
                        text = "Message",
                        icon = Icons.Default.Chat,
                        onClick = { showQuickReplySheet = true },
                        testTag = "call_quick_message_btn"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CallActionButton(
                            icon = Icons.Default.CallEnd,
                            label = "Decline",
                            backgroundColor = CallCrimson,
                            onClick = onDecline,
                            testTag = "call_decline_button"
                        )
                        CallActionButton(
                            icon = Icons.Default.Call,
                            label = "Accept",
                            backgroundColor = CallEmerald,
                            onClick = onAnswer,
                            testTag = "call_answer_button"
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(bottom = 14.dp)
                        .size(76.dp)
                        .background(CallCrimson, CircleShape)
                        .liquidGlassInteractive(
                            shape = CircleShape,
                            elevation = 4.dp,
                            isElevated = true,
                            testTag = "call_end_button",
                            onClick = onDecline
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        // Quick Decline SMS Bottom Sheet
        if (showQuickReplySheet) {
            val cannedReplies = listOf(
                "Can't talk right now. What's up?",
                "I'll call you right back.",
                "On my way.",
                "Sorry, in a meeting."
            )
            ModalBottomSheet(
                onDismissRequest = { showQuickReplySheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Color(0xFF1C1C1E)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Quick Decline with Message",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    cannedReplies.forEach { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showQuickReplySheet = false
                                    val number = callInfo?.number ?: ""
                                    if (number.isNotBlank()) {
                                        try {
                                            com.example.SalimApplication.instance.telecomRepository.openSms(number)
                                        } catch (_: Exception) {}
                                    }
                                    onDecline()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = msg, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun InCallTabSegment(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun InCallControlsGrid(
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isOnHold: Boolean,
    onMuteToggle: () -> Unit,
    onKeypadToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoCall: () -> Unit,
    onHoldToggle: () -> Unit,
    onNotesToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Row 1: Mute, Keypad, Speaker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InCallIconButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (isMuted) "Unmute" else "Mute",
                isActive = isMuted,
                onClick = onMuteToggle,
                testTag = "incall_mute_btn"
            )
            InCallIconButton(
                icon = Icons.Default.Dialpad,
                label = "Keypad",
                isActive = false,
                onClick = onKeypadToggle,
                testTag = "incall_keypad_btn"
            )
            InCallIconButton(
                icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                label = "Speaker",
                isActive = isSpeakerOn,
                onClick = onSpeakerToggle,
                testTag = "incall_speaker_btn"
            )
        }

        // Row 2: Video Call, Hold, Notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InCallIconButton(
                icon = Icons.Default.Videocam,
                label = "Video Call",
                isActive = false,
                onClick = onVideoCall,
                testTag = "incall_video_btn"
            )
            InCallIconButton(
                icon = Icons.Default.Pause,
                label = if (isOnHold) "Unhold" else "Hold",
                isActive = isOnHold,
                onClick = onHoldToggle,
                testTag = "incall_hold_btn"
            )
            InCallIconButton(
                icon = Icons.Default.EditNote,
                label = "Notes",
                isActive = false,
                onClick = onNotesToggle,
                testTag = "incall_notes_btn"
            )
        }
    }
}

@Composable
private fun InCallIconButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    if (isActive) Color.White else Color(0x38FFFFFF),
                    CircleShape
                )
                .liquidGlassInteractive(
                    shape = CircleShape,
                    elevation = if (isActive) 5.dp else 2.dp,
                    isElevated = isActive,
                    testTag = testTag,
                    onClick = onClick
                ),
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
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun InCallNotesView(
    noteText: String,
    onNoteTextChange: (String) -> Unit,
    onCopyNote: () -> Unit,
    onSaveNote: () -> Unit,
    onBackToControls: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = SalimBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "In-Call Note",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                TextButton(onClick = onBackToControls) {
                    Text("Controls", color = SalimBlue, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = onNoteTextChange,
                placeholder = {
                    Text(
                        "Write something the caller says...",
                        color = Color.White.copy(alpha = 0.45f)
                    )
                },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SalimBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                    cursorColor = SalimBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("incall_note_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy Button with icon + label
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (noteText.isNotBlank()) SalimBlue.copy(alpha = 0.2f) else Color.Transparent,
                    modifier = Modifier
                        .clickable(enabled = noteText.isNotBlank(), onClick = onCopyNote)
                        .testTag("copy_note_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (noteText.isNotBlank()) SalimBlue else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy Note",
                            color = if (noteText.isNotBlank()) SalimBlue else Color.White.copy(alpha = 0.3f),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Save Button
                Button(
                    onClick = onSaveNote,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SalimBlue,
                        contentColor = SalimWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = noteText.isNotBlank(),
                    modifier = Modifier.testTag("save_note_button")
                ) {
                    Text("Save Note")
                }
            }
        }
    }
}

@Composable
private fun InCallKeypadOverlay(
    enteredDigits: String,
    onDigitPress: (Char) -> Unit,
    onDigitRelease: () -> Unit,
    onBackspace: () -> Unit,
    onCopyDigits: () -> Unit,
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display box for typed digits with prominent Copy button and Backspace
        Row(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2C2C2E))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = enteredDigits.ifEmpty { "Enter digits..." },
                color = if (enteredDigits.isEmpty()) Color.White.copy(alpha = 0.4f) else Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )

            if (enteredDigits.isNotEmpty()) {
                // Prominent Copy Button with label & icon
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SalimBlue.copy(alpha = 0.2f),
                    modifier = Modifier
                        .clickable(onClick = onCopyDigits)
                        .testTag("copy_keypad_digits_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy digits",
                            tint = SalimBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copy",
                            color = SalimBlue,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Backspace Button
                IconButton(
                    onClick = onBackspace,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dialpad Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Back to Controls",
            color = SalimBlue,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .clickable(onClick = onClose)
                .padding(6.dp)
        )
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color = Color.Transparent,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(74.dp)
                .background(backgroundColor, CircleShape)
                .liquidGlassInteractive(
                    shape = CircleShape,
                    elevation = 4.dp,
                    isElevated = true,
                    testTag = testTag,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
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
