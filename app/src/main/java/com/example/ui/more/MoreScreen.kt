package com.example.ui.more

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.FrostCard
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass
import com.example.util.PermissionHelper
import com.example.util.RoleHelper

@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit,
    onRequestDefaultDialer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val isDefault = RoleHelper.isDefaultDialer(context)
    val hasCorePerms = PermissionHelper.hasContactsPermission(context) && PermissionHelper.hasCallLogPermission(context)

    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

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
            Text(
                text = "More",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = textPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Group 1: Features
            FrostCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    MoreNavRow(
                        icon = Icons.Default.Star,
                        title = "Favorites",
                        subtitle = "Starred and priority contacts",
                        onClick = { onNavigate(Screen.Favorites.route) },
                        testTag = "more_favorites"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MoreNavRow(
                        icon = Icons.Default.Voicemail,
                        title = "Voicemail",
                        subtitle = "Carrier voicemail speed-dial and inbox",
                        onClick = { onNavigate(Screen.Voicemail.route) },
                        testTag = "more_voicemail"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MoreNavRow(
                        icon = Icons.Default.Block,
                        title = "Blocked Numbers",
                        subtitle = "Spam screening and blocked list",
                        onClick = { onNavigate(Screen.BlockedNumbers.route) },
                        testTag = "more_blocked"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Group 2: System & Integration
            FrostCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    MoreNavRow(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Default Phone App",
                        subtitle = if (isDefault) "Salim is default dialer" else "Tap to set Salim as default dialer",
                        onClick = onRequestDefaultDialer,
                        testTag = "more_default_dialer"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MoreNavRow(
                        icon = Icons.Default.Security,
                        title = "App Permissions",
                        subtitle = if (hasCorePerms) "All core permissions granted" else "Some permissions need attention",
                        onClick = { onNavigate(Screen.Permissions.route) },
                        testTag = "more_permissions"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MoreNavRow(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        subtitle = "Preferences, sounds, haptics, and display",
                        onClick = { onNavigate(Screen.Settings.route) },
                        testTag = "more_settings"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Version Badge
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Salim Dialer • Version 1.0 (Liquid Glass Frost)",
                    style = MaterialTheme.typography.labelSmall,
                    color = textMuted.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MoreNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .liquidGlass(shape = CircleShape, elevation = 1.5.dp, isElevated = true),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = textMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(13.dp)
        )
    }
}
