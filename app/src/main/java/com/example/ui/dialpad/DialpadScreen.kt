package com.example.ui.dialpad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.usecase.PhoneNumberHelper
import com.example.ui.components.SalimAvatar
import com.example.ui.theme.CallEmerald
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostIconButton
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.GlassBackgroundLight
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.liquidGlassInteractive
import kotlinx.coroutines.delay

data class KeypadButtonDef(
    val digit: Char,
    val letters: String,
    val testTag: String
)

@Composable
fun DialpadScreen(
    viewModel: DialpadViewModel,
    onAddContact: (String) -> Unit,
    onViewContact: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val enteredNumber by viewModel.enteredNumber.collectAsState()
    val matchedContacts by viewModel.matchedContacts.collectAsState()
    val matchedSavedContact by viewModel.matchedSavedContact.collectAsState()

    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    val keyDefs = listOf(
        listOf(
            KeypadButtonDef('1', "", "dial_key_1"),
            KeypadButtonDef('2', "A B C", "dial_key_2"),
            KeypadButtonDef('3', "D E F", "dial_key_3")
        ),
        listOf(
            KeypadButtonDef('4', "G H I", "dial_key_4"),
            KeypadButtonDef('5', "J K L", "dial_key_5"),
            KeypadButtonDef('6', "M N O", "dial_key_6")
        ),
        listOf(
            KeypadButtonDef('7', "P Q R S", "dial_key_7"),
            KeypadButtonDef('8', "T U V", "dial_key_8"),
            KeypadButtonDef('9', "W X Y Z", "dial_key_9")
        ),
        listOf(
            KeypadButtonDef('*', "", "dial_key_star"),
            KeypadButtonDef('0', "+", "dial_key_0"),
            KeypadButtonDef('#', "", "dial_key_hash")
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: T9 Matches & Phone Number display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // T9 Matches Carousel
                AnimatedVisibility(
                    visible = matchedContacts.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        items(matchedContacts.take(5)) { match ->
                            val pillShape = RoundedCornerShape(18.dp)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .liquidGlassInteractive(
                                        shape = pillShape,
                                        elevation = 2.dp,
                                        testTag = "t9_match_${match.contact.id}",
                                        onClick = {
                                            val phone = match.matchedPhone ?: match.contact.primaryNumber
                                            viewModel.setNumber(phone)
                                        }
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SalimAvatar(
                                        name = match.contact.name,
                                        photoUri = match.contact.photoUri,
                                        size = 26.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = match.contact.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Number text formatted
                Text(
                    text = if (enteredNumber.isEmpty()) " " else PhoneNumberHelper.formatForDisplay(enteredNumber),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = if (enteredNumber.length > 11) 28.sp else 38.sp,
                        letterSpacing = 1.sp
                    ),
                    color = textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dial_number_display")
                )

                // Contact / Add to contacts affordance
                AnimatedVisibility(
                    visible = enteredNumber.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val savedContact = matchedSavedContact
                    if (savedContact != null) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .liquidGlassInteractive(
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = 2.dp,
                                    testTag = "dial_matched_contact_pill",
                                    onClick = { onViewContact(savedContact.id) }
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SalimAvatar(
                                    name = savedContact.name,
                                    photoUri = savedContact.photoUri,
                                    size = 28.dp
                                )
                                Column {
                                    Text(
                                        text = savedContact.name,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = textPrimary
                                    )
                                    val matchedType = savedContact.numbers.firstOrNull {
                                        PhoneNumberHelper.areNumbersEqual(it.number, enteredNumber) ||
                                        PhoneNumberHelper.areNumbersEqual(it.normalizedNumber, enteredNumber)
                                    }?.type ?: "Saved Contact"
                                    Text(
                                        text = "$matchedType • Tap to view",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "View contact",
                                    tint = textMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    } else if (enteredNumber.length >= 3) {
                        Box(modifier = Modifier.padding(top = 8.dp)) {
                            FrostButton(
                                text = "Add to Contacts",
                                icon = Icons.Default.PersonAdd,
                                onClick = { onAddContact(enteredNumber) },
                                testTag = "dial_add_contact_button"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Keypad Grid (3x4)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keyDefs.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { def ->
                            DialpadGlassKey(
                                def = def,
                                onClick = { viewModel.appendDigit(def.digit) },
                                onLongClick = {
                                    if (def.digit == '0') {
                                        viewModel.appendDigit('+')
                                    } else if (def.digit == '1') {
                                        // Voicemail shortcut
                                        viewModel.makeCall()
                                    }
                                }
                            )
                        }
                    }
                }

                // Action Row: Clear spacer, Pure Neutral Glass Call button, Backspace
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left empty slot for symmetry
                    Box(modifier = Modifier.size(74.dp))

                    // High-contrast Apple & Google style Frosted Emerald Call Button
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .liquidGlassInteractive(
                                shape = CircleShape,
                                elevation = 4.dp,
                                isElevated = true,
                                testTag = "dial_call_button",
                                onClick = { viewModel.makeCall() }
                            )
                            .background(CallEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Backspace button
                    Box(
                        modifier = Modifier.size(74.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (enteredNumber.isNotEmpty()) {
                            FrostIconButton(
                                icon = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Delete digit",
                                onClick = { viewModel.deleteLastDigit() },
                                size = 56.dp,
                                iconSize = 26.dp,
                                testTag = "dial_backspace_button"
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Authentic Circular Liquid Glass Key with Specular Rim, Spring Compression, and Tactile Haptics.
 */
@Composable
fun DialpadGlassKey(
    def: KeypadButtonDef,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight
    val haptic = LocalHapticFeedback.current

    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(74.dp)
            .scale(if (isPressed) 0.96f else 1.0f)
            .liquidGlass(
                shape = CircleShape,
                elevation = if (isPressed) 1.dp else 2.5.dp,
                isElevated = isPressed
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                    // Check for long press (e.g. 500ms)
                    var isLongPress = false
                    val start = System.currentTimeMillis()
                    while (true) {
                        val event = awaitPointerEvent()
                        if (System.currentTimeMillis() - start > 450L && !isLongPress) {
                            isLongPress = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                            break
                        }
                        if (event.changes.any { it.isConsumed || !it.pressed }) {
                            break
                        }
                    }

                    isPressed = false
                    if (!isLongPress) {
                        onClick()
                    }
                }
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
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 30.sp
                ),
                color = textPrimary
            )
            if (def.letters.isNotEmpty()) {
                Text(
                    text = def.letters,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.1.sp,
                        fontSize = 10.sp
                    ),
                    color = textMuted
                )
            }
        }
    }
}

@Composable
fun DialpadKey(
    def: KeypadButtonDef,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    DialpadGlassKey(
        def = def,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
    )
}
