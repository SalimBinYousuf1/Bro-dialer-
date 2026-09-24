package com.example.ui.dialpad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.usecase.PhoneNumberHelper
import com.example.ui.components.SalimAvatar
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimKeypadBackground
import com.example.ui.theme.SalimWhite

data class KeypadButtonDef(
    val digit: Char,
    val letters: String,
    val testTag: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialpadScreen(
    viewModel: DialpadViewModel,
    onAddContact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val enteredNumber by viewModel.enteredNumber.collectAsState()
    val matchedContacts by viewModel.matchedContacts.collectAsState()

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

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: T9 Matches & Number display
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
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .testTag("t9_match_${match.contact.id}"),
                                onClick = {
                                    val phone = match.matchedPhone ?: match.contact.primaryNumber
                                    viewModel.setNumber(phone)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SalimAvatar(
                                        name = match.contact.name,
                                        photoUri = match.contact.photoUri,
                                        size = 28.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = match.contact.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Number text
                Text(
                    text = if (enteredNumber.isEmpty()) " " else PhoneNumberHelper.formatForDisplay(enteredNumber),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = if (enteredNumber.length > 11) 28.sp else 36.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dial_number_display")
                )

                // Add to contacts affordance
                AnimatedVisibility(
                    visible = enteredNumber.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(
                        onClick = { onAddContact(enteredNumber) },
                        modifier = Modifier.testTag("dial_add_contact_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = SalimBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Number",
                            style = MaterialTheme.typography.labelLarge,
                            color = SalimBlue
                        )
                    }
                }
            }

            // Keypad Grid (3x4)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keyDefs.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { def ->
                            DialpadKey(
                                def = def,
                                onClick = { viewModel.appendDigit(def.digit) },
                                onLongClick = {
                                    if (def.digit == '0') {
                                        viewModel.appendDigit('+')
                                    }
                                }
                            )
                        }
                    }
                }

                // Action Row: Empty spacer, Call button, Backspace
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left empty slot for symmetry
                    Box(modifier = Modifier.size(76.dp))

                    // Green Call Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(SalimGreen)
                            .clickable(
                                onClick = { viewModel.makeCall() }
                            )
                            .testTag("dial_call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = SalimWhite,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Backspace / Clear button
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (enteredNumber.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.deleteLastDigit() },
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("dial_backspace_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete digit",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialpadKey(
    def: KeypadButtonDef,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (def.letters.isNotEmpty()) {
                Text(
                    text = def.letters,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
