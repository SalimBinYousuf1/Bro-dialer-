package com.example.ui.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallType
import com.example.data.model.ContactItem
import com.example.ui.components.SalimAvatar
import com.example.ui.navigation.Screen
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostCard
import com.example.ui.theme.FrostIconButton
import com.example.ui.theme.FrostInteractiveCard
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.liquidGlassInteractive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit,
    onRequestDefaultDialer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val state by viewModel.state.collectAsState()

    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // App Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Salim",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Native Phone & Dialer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .liquidGlass(shape = CircleShape, elevation = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = "Status",
                        tint = textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Default Dialer Banner
            if (!state.isDefaultDialer) {
                FrostCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("default_dialer_banner")
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .liquidGlass(shape = CircleShape, elevation = 2.dp, isElevated = true),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Set as Default Phone App",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Set Salim as your default dialer to enable full incoming call screens, spam screening, and call control.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        FrostButton(
                            text = "Make Salim Default",
                            onClick = onRequestDefaultDialer,
                            isProminent = true,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "make_default_button"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = textPrimary,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeQuickActionTile(
                    icon = Icons.Default.Dialpad,
                    label = "Keypad",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Dialpad.route) }
                )
                HomeQuickActionTile(
                    icon = Icons.Default.PersonAdd,
                    label = "New Contact",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.ContactEdit.createRoute(-1L)) }
                )
                HomeQuickActionTile(
                    icon = Icons.Default.History,
                    label = "Recents",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Recents.route) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeQuickActionTile(
                    icon = Icons.Default.Star,
                    label = "Favorites",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Favorites.route) }
                )
                HomeQuickActionTile(
                    icon = Icons.Default.Voicemail,
                    label = "Voicemail",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Voicemail.route) }
                )
                HomeQuickActionTile(
                    icon = Icons.Default.Block,
                    label = "Blocked",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.BlockedNumbers.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Favorites Carousel
            if (state.favorites.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Favorite Contacts",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = textPrimary
                    )
                    FrostButton(
                        text = "View All",
                        onClick = { onNavigate(Screen.Favorites.route) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.favorites) { contact ->
                        FavoriteGlassChip(
                            contact = contact,
                            onClick = {
                                if (contact.primaryNumber.isNotBlank()) {
                                    viewModel.makeCall(contact.primaryNumber)
                                } else {
                                    onNavigate(Screen.ContactDetail.createRoute(contact.id))
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recent Calls Activity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = textPrimary
                )
                FrostButton(
                    text = "All Calls",
                    onClick = { onNavigate(Screen.Recents.route) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.recentCalls.isEmpty()) {
                FrostCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No recent calls logged yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textMuted,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.recentCalls.take(4).forEach { record ->
                        FrostInteractiveCard(
                            onClick = { viewModel.makeCall(record.number) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SalimAvatar(
                                    name = record.callerName ?: record.number,
                                    photoUri = record.photoUri,
                                    size = 42.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.callerName ?: record.number,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        ),
                                        color = textPrimary,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val typeIcon = when (record.type) {
                                            CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                                            CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                                            CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed
                                            else -> Icons.Default.Call
                                        }
                                        Icon(
                                            imageVector = typeIcon,
                                            contentDescription = null,
                                            tint = textMuted,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(record.date))
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textMuted
                                        )
                                    }
                                }

                                FrostIconButton(
                                    icon = Icons.Default.Call,
                                    contentDescription = "Call back",
                                    onClick = { viewModel.makeCall(record.number) },
                                    size = 36.dp,
                                    iconSize = 18.dp,
                                    elevation = 1.dp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HomeQuickActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .height(96.dp)
            .liquidGlassInteractive(
                shape = shape,
                elevation = 2.5.dp,
                onClick = onClick
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .liquidGlass(shape = CircleShape, elevation = 1.5.dp, isElevated = true),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = textPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FavoriteGlassChip(
    contact: ContactItem,
    onClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .width(90.dp)
            .height(106.dp)
            .liquidGlassInteractive(shape = shape, elevation = 2.dp, onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SalimAvatar(
                name = contact.name,
                photoUri = contact.photoUri,
                size = 46.dp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = contact.name.split(" ").firstOrNull() ?: contact.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = textPrimary,
                maxLines = 1
            )
        }
    }
}
