package com.example.ui.permissions

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SalimBackButton
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostCard
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass
import com.example.util.PermissionHelper

@Composable
fun PermissionsScreen(
    onRequestPermissions: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val hasContacts = PermissionHelper.hasContactsPermission(context)
    val hasCallLog = PermissionHelper.hasCallLogPermission(context)

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SalimBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Permissions",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .liquidGlass(shape = CircleShape, elevation = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = textPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Privacy & On-Device Security",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Salim processes your calls, call logs, and contacts strictly on-device. No telemetry or personal data ever leaves your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            FrostCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PermissionStatusItem(
                        icon = Icons.Default.Person,
                        title = "Contacts",
                        description = "Display contact names, photos, and match numbers in dialpad",
                        isGranted = hasContacts
                    )
                    PermissionStatusItem(
                        icon = Icons.Default.History,
                        title = "Call Log",
                        description = "Show recent incoming, outgoing, and missed call history",
                        isGranted = hasCallLog
                    )
                    PermissionStatusItem(
                        icon = Icons.Default.Call,
                        title = "Phone & Calling",
                        description = "Place calls and interact with system telecom manager",
                        isGranted = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FrostButton(
                text = "Open App Settings",
                onClick = { PermissionHelper.openAppSettings(context) },
                isProminent = true,
                modifier = Modifier.fillMaxWidth(),
                testTag = "open_settings_permissions"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PermissionStatusItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    Row(
        modifier = Modifier.fillMaxWidth(),
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
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = textPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .liquidGlass(shape = RoundedCornerShape(8.dp), elevation = 1.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isGranted) "Granted" else "Missing",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textPrimary
            )
        }
    }
}
