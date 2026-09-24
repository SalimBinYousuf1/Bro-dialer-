package com.example.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.SalimBackButton
import com.example.ui.components.SalimTopAppBar
import com.example.ui.navigation.Screen
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimRed
import com.example.ui.theme.SalimWhite
import com.example.util.PermissionHelper
import com.example.util.RoleHelper

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigate: (String) -> Unit,
    onRequestDefaultDialer: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isDefaultDialer = RoleHelper.isDefaultDialer(context)
    val hasCorePerms = PermissionHelper.hasContactsPermission(context) && PermissionHelper.hasCallLogPermission(context)

    var showDefaultTabDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCallBackgroundUri(uri.toString())
        }
    }

    val tabDisplayNames = mapOf(
        "home" to "Home",
        "recents" to "Recents",
        "contacts" to "Contacts",
        "dialpad" to "Keypad",
        "more" to "More"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SalimTopAppBar(
                title = "Settings",
                navigationIcon = {
                    SalimBackButton(onClick = onBack)
                }
            )
        }
    ) { padding ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Section 1: System Permissions
                SettingsSectionHeader("SYSTEM PERMISSIONS")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsClickableRow(
                            icon = Icons.Default.Security,
                            title = "Permissions",
                            subtitle = if (hasCorePerms) "All essential permissions granted" else "Tap to review system permissions",
                            badge = if (hasCorePerms) "Granted" else "Action Needed",
                            badgeColor = if (hasCorePerms) SalimGreen else SalimBlue,
                            onClick = { PermissionHelper.openAppSettings(context) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Navigation & Default Tab
                SettingsSectionHeader("NAVIGATION & DEFAULT SCREEN")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsClickableRow(
                            icon = Icons.Default.Home,
                            title = "Default Launch Screen",
                            subtitle = "Screen displayed when Salim opens",
                            badge = tabDisplayNames[settings.defaultStartTab] ?: "Home",
                            badgeColor = SalimBlue,
                            onClick = { showDefaultTabDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 3: Calling & Dialpad
                SettingsSectionHeader("CALLING & DIALPAD")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsSwitchRow(
                            icon = Icons.Default.Dialpad,
                            title = "T9 Predictive Search",
                            subtitle = "Match contacts as you dial digits",
                            checked = settings.t9SearchEnabled,
                            onCheckedChange = viewModel::toggleT9Search,
                            testTag = "setting_t9_search"
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsSwitchRow(
                            icon = Icons.Default.VolumeUp,
                            title = "Dialpad DTMF Tones",
                            subtitle = "Audible touch tones when dialing",
                            checked = settings.dialpadTones,
                            onCheckedChange = viewModel::toggleDialpadTones,
                            testTag = "setting_dtmf_tones"
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsSwitchRow(
                            icon = Icons.Default.Vibration,
                            title = "Haptic Feedback",
                            subtitle = "Subtle vibration on key taps",
                            checked = settings.hapticFeedback,
                            onCheckedChange = viewModel::toggleHapticFeedback,
                            testTag = "setting_haptic"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 4: Calling Screen Wallpaper / Background
                SettingsSectionHeader("CALL SCREEN WALLPAPER")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "In-Call Wallpaper",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (settings.callBackgroundUri != null) "Custom photo wallpaper active" else "Default Dark Glass aesthetic",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Preview Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(54.dp, 72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1C1C1E))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!settings.callBackgroundUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = settings.callBackgroundUri,
                                        contentDescription = "Wallpaper Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Wallpaper,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SalimBlue,
                                    contentColor = SalimWhite
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Photo")
                            }

                            if (settings.callBackgroundUri != null) {
                                Spacer(modifier = Modifier.width(10.dp))
                                TextButton(
                                    onClick = { viewModel.setCallBackgroundUri(null) }
                                ) {
                                    Text("Reset", color = SalimRed, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 5: Call Screening & Protection
                SettingsSectionHeader("CALL SCREENING & BLOCKING")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsClickableRow(
                            icon = Icons.Default.Block,
                            title = "Blocked Numbers",
                            subtitle = "Manage numbers blocked from calling",
                            onClick = { onNavigate(Screen.BlockedNumbers.route) }
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsSwitchRow(
                            icon = Icons.Default.Security,
                            title = "Block Unknown Callers",
                            subtitle = "Silence calls from numbers not in your contacts",
                            checked = settings.blockUnknownNumbers,
                            onCheckedChange = viewModel::toggleBlockUnknown,
                            testTag = "setting_block_unknown"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 6: Appearance
                SettingsSectionHeader("APPEARANCE")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsSwitchRow(
                            icon = Icons.Default.DarkMode,
                            title = "Dark Theme",
                            subtitle = "Clean high-contrast dark aesthetic",
                            checked = settings.darkTheme,
                            onCheckedChange = viewModel::toggleDarkTheme,
                            testTag = "setting_dark_theme"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 7: About Salim
                SettingsSectionHeader("ABOUT SALIM")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SalimBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Salim Dialer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Version 1.0 (Production Native Android)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pure native Kotlin and Jetpack Compose dialer. Built with Android Telecom APIs, ContactsContract, Room database, and modern unidirectional architecture.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Dialog for Selecting Default Start Tab
        if (showDefaultTabDialog) {
            val tabs = listOf(
                "home" to "Home (Dashboard & Favorites)",
                "recents" to "Recents (Call History)",
                "contacts" to "Contacts (Address Book)",
                "dialpad" to "Keypad (Phone Dialpad)",
                "more" to "More (Hub & Tools)"
            )

            AlertDialog(
                onDismissRequest = { showDefaultTabDialog = false },
                title = {
                    Text(
                        text = "Default Launch Screen",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Choose which screen opens automatically whenever you launch Salim:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        tabs.forEach { (tabKey, tabLabel) ->
                            val isSelected = settings.defaultStartTab == tabKey
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setDefaultStartTab(tabKey)
                                            showDefaultTabDialog = false
                                        }
                                    )
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) SalimBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tabLabel,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) SalimBlue else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDefaultTabDialog = false }) {
                        Text("Done", color = SalimBlue, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = SalimBlue,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SalimBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = badgeColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SalimBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SalimWhite,
                checkedTrackColor = SalimGreen,
                uncheckedThumbColor = SalimWhite,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
