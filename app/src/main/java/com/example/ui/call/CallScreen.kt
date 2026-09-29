package com.example.ui.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.ui.dialpad.KeypadButtonDef
import com.example.ui.theme.CallCrimson
import com.example.ui.theme.CallEmerald
import com.example.ui.theme.FrostButton
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimWhite
import com.example.ui.theme.liquidGlass
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
    quickMessages: List<String> = emptyList(),
    isInPipMode: Boolean = false,
    onUpdateQuickMessages: (List<String>) -> Unit = {},
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onMuteToggle: (Boolean) -> Unit,
    onSpeakerToggle: () -> Unit,
    onHoldToggle: () -> Unit,
    onVideoCall: () -> Unit,
    onToggleRecord: () -> Unit = {},
    onDtmfTone: (Char) -> Unit,
    onDtmfStop: () -> Unit,
    onSaveNote: (number: String, name: String?, note: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    if (isInPipMode) {
        PipCallLayout(
            callInfo = callInfo,
            onDecline = onDecline,
            onMuteToggle = { onMuteToggle(callInfo?.isMuted ?: false) },
            onSpeakerToggle = onSpeakerToggle
        )
        return
    }

    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var activeTab by remember { mutableStateOf(InCallTab.CONTROLS) }
    var keypadDigits by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var copyNotice by remember { mutableStateOf<String?>(null) }
    var showQuickReplySheet by remember { mutableStateOf(false) }
    var isEditingQuickMessages by remember { mutableStateOf(false) }
    var customReplyText by remember { mutableStateOf("") }
    var editableMessages by remember(quickMessages) { mutableStateOf(quickMessages.toMutableList()) }

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
        // High-contrast deep luxury dark backdrop (Apple phone style)
        if (!backgroundUri.isNullOrBlank()) {
            AsyncImage(
                model = backgroundUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.72f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF141416))
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
                // Caller Avatar (Photo or Initial Avatar)
                if (!callInfo?.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = callInfo.photoUri,
                        contentDescription = "Caller Photo",
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    SalimAvatar(
                        name = callInfo?.displayName ?: "Unknown",
                        size = 88.dp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Caller Name / Headline
                Text(
                    text = if (callInfo?.isSavedContact == true) {
                        callInfo.displayName.ifBlank { "Unknown Caller" }
                    } else if (!callInfo?.number.isNullOrBlank()) {
                        PhoneNumberHelper.formatForDisplay(callInfo.number)
                    } else {
                        callInfo?.displayName ?: "Unknown Caller"
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(3.dp))

                // Subtitle / Contact distinction
                if (callInfo?.isSavedContact == true) {
                    if (!callInfo.number.isNullOrBlank()) {
                        Text(
                            text = PhoneNumberHelper.formatForDisplay(callInfo.number),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "Unsaved Caller",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (state == TelephonyCallState.RINGING) SalimGreen else Color.White.copy(alpha = 0.9f)
                )

                // Live Call Recording Indicator
                if (callInfo?.isRecording == true) {
                    val recMins = (callInfo.recordingDurationSeconds) / 60
                    val recSecs = (callInfo.recordingDurationSeconds) % 60
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE53935))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REC %02d:%02d".format(recMins, recSecs),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // Sleek Apple Pill Segmented Tab Control (Controls | Notes | Keypad)
                if (state == TelephonyCallState.ACTIVE || state == TelephonyCallState.HOLDING || state == TelephonyCallState.DIALING) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
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
                            .background(Color(0xFF2C2C2E))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = copyNotice ?: "",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.5f))

            // Dynamic Content based on Active Tab
            if (state == TelephonyCallState.ACTIVE || state == TelephonyCallState.HOLDING || state == TelephonyCallState.DIALING) {
                when (activeTab) {
                    InCallTab.CONTROLS -> {
                        InCallControlsGrid(
                            isMuted = callInfo?.isMuted ?: false,
                            isSpeakerOn = callInfo?.isSpeakerOn ?: false,
                            isOnHold = callInfo?.isOnHold ?: false,
                            isRecording = callInfo?.isRecording ?: false,
                            recordingDurationSeconds = callInfo?.recordingDurationSeconds ?: 0L,
                            onMuteToggle = { onMuteToggle(callInfo?.isMuted ?: false) },
                            onKeypadToggle = { activeTab = InCallTab.KEYPAD },
                            onSpeakerToggle = onSpeakerToggle,
                            onRecordToggle = onToggleRecord,
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
                                    copyNotice = "Saved to Notes app"
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
                                    copyNotice = "Keypad digits copied"
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
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quick reply canned SMS decline button
                    FrostButton(
                        text = "Quick Message",
                        icon = Icons.Default.Chat,
                        onClick = { showQuickReplySheet = true },
                        testTag = "call_quick_message_btn"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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

        // Quick Decline SMS Bottom Sheet with 4 Persistent Customizable Messages
        if (showQuickReplySheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showQuickReplySheet = false
                    isEditingQuickMessages = false
                },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Color(0xFF1C1C1E)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditingQuickMessages) "Customize Messages" else "Quick Decline with Message",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        TextButton(onClick = { isEditingQuickMessages = !isEditingQuickMessages }) {
                            Text(
                                text = if (isEditingQuickMessages) "Done" else "Edit",
                                color = SalimBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditingQuickMessages) {
                        editableMessages.forEachIndexed { index, msg ->
                            OutlinedTextField(
                                value = msg,
                                onValueChange = { newText ->
                                    val updated = editableMessages.toMutableList()
                                    updated[index] = newText
                                    editableMessages = updated
                                    onUpdateQuickMessages(updated)
                                },
                                label = { Text("Message ${index + 1}", color = Color.White.copy(alpha = 0.7f)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = SalimBlue,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    cursorColor = SalimBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    } else {
                        editableMessages.forEach { msg ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable {
                                        showQuickReplySheet = false
                                        val number = callInfo?.number ?: ""
                                        if (number.isNotBlank()) {
                                            try {
                                                com.example.SalimApplication.instance.telecomRepository.sendDirectSms(number, msg)
                                            } catch (_: Exception) {}
                                        }
                                        onDecline()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = SalimBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Or write custom reply:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customReplyText,
                                onValueChange = { customReplyText = it },
                                placeholder = {
                                    Text(
                                        "Write message...",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = SalimBlue,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    cursorColor = SalimBlue
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (customReplyText.isNotBlank()) {
                                        val number = callInfo?.number ?: ""
                                        if (number.isNotBlank()) {
                                            try {
                                                com.example.SalimApplication.instance.telecomRepository.sendDirectSms(number, customReplyText)
                                            } catch (_: Exception) {}
                                        }
                                        showQuickReplySheet = false
                                        onDecline()
                                    }
                                },
                                enabled = customReplyText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = SalimBlue)
                            ) {
                                Text("Send", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
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
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun InCallControlsGrid(
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isOnHold: Boolean,
    isRecording: Boolean,
    recordingDurationSeconds: Long,
    onMuteToggle: () -> Unit,
    onKeypadToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onRecordToggle: () -> Unit,
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

        // Row 2: Record, Hold, Notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val recLabel = if (isRecording) {
                val mins = recordingDurationSeconds / 60
                val secs = recordingDurationSeconds % 60
                "%02d:%02d".format(mins, secs)
            } else "Record"

            InCallIconButton(
                icon = Icons.Default.Mic,
                label = recLabel,
                isActive = isRecording,
                activeColor = Color(0xFFE53935),
                onClick = onRecordToggle,
                testTag = "incall_record_btn"
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

        // Row 3: Video Call (compact quick action)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            InCallIconButton(
                icon = Icons.Default.Videocam,
                label = "Video Call",
                isActive = false,
                onClick = onVideoCall,
                testTag = "incall_video_btn"
            )
        }
    }
}

@Composable
private fun InCallIconButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = Color.White,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        val isCustomColor = activeColor != Color.White
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    if (isActive) activeColor else Color(0x38FFFFFF),
                    CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (isActive) activeColor else Color.White.copy(alpha = 0.25f),
                    shape = CircleShape
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
                tint = if (isActive) (if (isCustomColor) Color.White else Color.Black) else Color.White,
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
            color = Color.White
        )
    }
}

@Composable
private fun PipCallLayout(
    callInfo: ActiveCallInfo?,
    onDecline: () -> Unit,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            SalimAvatar(
                name = callInfo?.displayName?.ifBlank { callInfo.number } ?: "Caller",
                photoUri = callInfo?.photoUri,
                size = 44.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = callInfo?.displayName?.ifBlank { callInfo.number } ?: "Call",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "In Call",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF34C759)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onMuteToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (callInfo?.isMuted == true) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (callInfo?.isMuted == true) Color(0xFFFF3B30) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30))
                        .clickable(onClick = onDecline),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onSpeakerToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (callInfo?.isSpeakerOn == true) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = if (callInfo?.isSpeakerOn == true) Color(0xFF34C759) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * High-Contrast, crystal clear In-Call Notes Card
 */
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
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF222226)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.22f)))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "In-Call Note",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White
                    )
                }

                TextButton(onClick = onBackToControls) {
                    Text("Controls", color = SalimBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = onNoteTextChange,
                placeholder = {
                    Text(
                        "Write caller details, address, meeting notes...",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SalimBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
                    cursorColor = SalimBlue,
                    focusedContainerColor = Color(0xFF1A1A1E),
                    unfocusedContainerColor = Color(0xFF1A1A1E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("incall_note_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy Button with pure high contrast
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.25f))),
                    modifier = Modifier
                        .clickable(enabled = noteText.isNotBlank(), onClick = onCopyNote)
                        .testTag("copy_note_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Save to Notes App Button
                Button(
                    onClick = onSaveNote,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SalimBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = noteText.isNotBlank(),
                    modifier = Modifier.testTag("save_note_button")
                ) {
                    Text("Save to Notes App", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * High-Contrast, crystal clear In-Call Keypad with bold white digits on dark glass keys
 */
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
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High contrast display bar for typed DTMF digits
        Row(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF26262A))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = enteredDigits.ifEmpty { "Enter DTMF digits..." },
                color = if (enteredDigits.isEmpty()) Color.White.copy(alpha = 0.5f) else Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                modifier = Modifier.weight(1f)
            )

            if (enteredDigits.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SalimBlue,
                    modifier = Modifier
                        .clickable(onClick = onCopyDigits)
                        .testTag("copy_keypad_digits_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy digits",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copy",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onBackspace,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // High Contrast Keys
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { def ->
                        InCallGlassKey(
                            def = def,
                            onClick = { onDigitPress(def.digit) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Back to Controls",
            color = SalimBlue,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
            modifier = Modifier
                .clickable(onClick = onClose)
                .padding(6.dp)
        )
    }
}

/**
 * Authentic In-Call Glass Key with pure white high-contrast text and border
 */
@Composable
private fun InCallGlassKey(
    def: KeypadButtonDef,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.32f), CircleShape)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .testTag(def.testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = def.digit.toString(),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp
                ),
                color = Color.White
            )
            if (def.letters.isNotEmpty()) {
                Text(
                    text = def.letters,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        fontSize = 10.sp
                    ),
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }
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
                .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
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
