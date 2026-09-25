package com.example.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.SalimBackButton
import com.example.ui.navigation.Screen
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostCard
import com.example.ui.theme.FrostSwitch
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.GlassBackgroundLight
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass
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
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isDefaultDialer = RoleHelper.isDefaultDialer(context)
    val hasCorePerms = PermissionHelper.hasContactsPermission(context) && PermissionHelper.hasCallLogPermission(context)

    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SalimBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Section 1: System Permissions & Default Role
            SettingsSectionHeader("CORE TELEPHONY")
            FrostCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsClickableRow(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Default Phone App",
                        subtitle = if (isDefaultDialer) "Salim is default phone dialer" else "Tap to set Salim as default dialer",
                        badge = if (isDefaultDialer) "Active" else "Setup",
                        onClick = onRequestDefaultDialer
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SettingsClickableRow(
                        icon = Icons.Default.Security,
                        title = "System Permissions",
                        subtitle = if (hasCorePerms) "All essential permissions granted" else "Tap to grant missing permissions",
                        badge = if (hasCorePerms) "Granted" else "Action Needed",
                        onClick = { PermissionHelper.openAppSettings(context) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Navigation & Default Tab
            SettingsSectionHeader("NAVIGATION & STARTUP")
            FrostCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsClickableRow(
                        icon = Icons.Default.Home,
                        title = "Default Startup Screen",
                        subtitle = "Screen opened on launch",
                        badge = tabDisplayNames[settings.defaultStartTab] ?: "Home",
                        onClick = { showDefaultTabDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Dialpad & Calls (Switches)
            SettingsSectionHeader("DIALPAD & FEEDBACK")
            FrostCard(modifier = Modifier.fillMaxWidth()) {
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
                        color = textMuted.copy(alpha = 0.2f),
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
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SettingsSwitchRow(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Feedback",
                        subtitle = "Gentle physical tap on keypad presses",
                        checked = settings.hapticFeedback,
                        onCheckedChange = viewModel::toggleHapticFeedback,
                        testTag = "setting_haptic"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SettingsSwitchRow(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Theme",
                        subtitle = "Obsidian dark liquid glass material",
                        checked = settings.darkTheme,
                        onCheckedChange = viewModel::toggleDarkTheme,
                        testTag = "setting_dark_theme"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Call Screen Wallpaper / Background
            SettingsSectionHeader("CALL SCREEN APPEARANCE")
            FrostCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Call Background Wallpaper",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = textPrimary
                            )
                            Text(
                                text = if (settings.callBackgroundUri != null) "Custom wallpaper active" else "Default liquid glass frost",
                                style = MaterialTheme.typography.bodySmall,
                                color = textMuted
                            )
                        }

                        if (settings.callBackgroundUri != null) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = settings.callBackgroundUri,
                                    contentDescription = "Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FrostButton(
                            text = "Choose Image",
                            icon = Icons.Default.PhotoLibrary,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "setting_choose_wallpaper"
                        )

                        if (settings.callBackgroundUri != null) {
                            FrostButton(
                                text = "Reset",
                                onClick = { viewModel.setCallBackgroundUri(null) },
                                modifier = Modifier.weight(1f),
                                testTag = "setting_reset_wallpaper"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 5: Blocked Numbers & Spam
            SettingsSectionHeader("SECURITY & BLOCKING")
            FrostCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsClickableRow(
                        icon = Icons.Default.Block,
                        title = "Blocked Numbers",
                        subtitle = "Manage blocked caller list",
                        onClick = { onNavigate(Screen.BlockedNumbers.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Default Startup Tab Selector Dialog
    if (showDefaultTabDialog) {
        val tabKeys = listOf("home", "recents", "contacts", "dialpad", "more")
        AlertDialog(
            onDismissRequest = { showDefaultTabDialog = false },
            title = {
                Text(
                    text = "Default Startup Screen",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column {
                    tabKeys.forEach { key ->
                        val name = tabDisplayNames[key] ?: key
                        val isSelected = settings.defaultStartTab == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setDefaultStartTab(key)
                                        showDefaultTabDialog = false
                                    }
                                )
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                FrostButton(
                    text = "Cancel",
                    onClick = { showDefaultTabDialog = false }
                )
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val dark = isSystemInDarkTheme()
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 11.sp
        ),
        color = textMuted,
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .liquidGlass(shape = CircleShape, elevation = 1.5.dp, isElevated = true),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textPrimary,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                color = textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
            )
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .liquidGlass(shape = RoundedCornerShape(8.dp), elevation = 1.dp)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = textPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = textMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(13.dp)
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
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .liquidGlass(shape = CircleShape, elevation = 1.5.dp, isElevated = true),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textPrimary,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                color = textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
            )
        }
        FrostSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
