package com.example.ui.more

import android.content.Context
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimRed
import com.example.ui.theme.SalimYellow
import com.example.util.PermissionHelper
import com.example.util.RoleHelper

@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit,
    onRequestDefaultDialer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDefault = RoleHelper.isDefaultDialer(context)

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "More",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Navigation Group 1: Features
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    MoreNavRow(
                        icon = Icons.Default.Star,
                        iconTint = SalimYellow,
                        title = "Favorites",
                        subtitle = "Frequently contacted numbers",
                        onClick = { onNavigate(Screen.Favorites.route) },
                        testTag = "more_favorites"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MoreNavRow(
                        icon = Icons.Default.Voicemail,
                        iconTint = SalimBlue,
                        title = "Voicemail",
                        subtitle = "Carrier voicemail access",
                        onClick = { onNavigate(Screen.Voicemail.route) },
                        testTag = "more_voicemail"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MoreNavRow(
                        icon = Icons.Default.Block,
                        iconTint = SalimRed,
                        title = "Blocked Numbers",
                        subtitle = "Manage call blocklist",
                        onClick = { onNavigate(Screen.BlockedNumbers.route) },
                        testTag = "more_blocked"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Group 2: System & Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    MoreNavRow(
                        icon = Icons.Default.Settings,
                        iconTint = Color(0xFF6E6E73),
                        title = "Settings",
                        subtitle = "T9 search, tones, appearance",
                        onClick = { onNavigate(Screen.Settings.route) },
                        testTag = "more_settings"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MoreNavRow(
                        icon = Icons.Default.Phone,
                        iconTint = SalimBlue,
                        title = "Contacts Settings",
                        subtitle = "Display, accounts, duplicate merge, trash",
                        onClick = { onNavigate(Screen.ContactsSettings.route) },
                        testTag = "more_contacts_settings"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MoreNavRow(
                        icon = Icons.Default.PhoneAndroid,
                        iconTint = if (isDefault) SalimGreen else SalimBlue,
                        title = "Default Phone App",
                        subtitle = if (isDefault) "Salim is default" else "Set as default dialer",
                        badge = if (isDefault) "Default" else "Configure",
                        badgeColor = if (isDefault) SalimGreen else SalimBlue,
                        onClick = onRequestDefaultDialer,
                        testTag = "more_default_role"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MoreNavRow(
                        icon = Icons.Default.Security,
                        iconTint = SalimBlue,
                        title = "Permissions",
                        subtitle = "System permissions & privacy",
                        onClick = { PermissionHelper.openAppSettings(context) },
                        testTag = "more_permissions"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Group 3: Emergency assistance info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Sos, contentDescription = null, tint = SalimRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Emergency Services",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Emergency numbers (such as 911, 112, 999) can be dialed anytime from the Keypad tab regardless of SIM or lock state according to Android carrier safety standards.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MoreNavRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = SalimBlue,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (badge != null) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = badgeColor,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}
