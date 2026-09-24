package com.example.ui.contacts

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.telephony.CallManager
import com.example.ui.components.SalimAvatar
import com.example.ui.components.SalimAvatarPickerSheet
import com.example.ui.components.SalimBackButton
import com.example.ui.components.SalimConfirmationDialog
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimRed
import com.example.ui.theme.SalimYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId: Long,
    viewModel: ContactsViewModel,
    onBack: () -> Unit,
    onEditContact: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    // Dialogs for preferences
    var showRingtoneDialog by remember { mutableStateOf(false) }
    var showCallBackgroundDialog by remember { mutableStateOf(false) }
    var showSimDialog by remember { mutableStateOf(false) }

    // Custom Contact Settings state
    var selectedRingtone by remember { mutableStateOf("Follow system") }
    var selectedBackground by remember { mutableStateOf("Contact photo priority") }
    var selectedSim by remember { mutableStateOf("Follow system") }

    // Call history dropdown
    var callHistoryExpanded by remember { mutableStateOf(false) }
    var contactCallLogs by remember { mutableStateOf<List<CallRecord>>(emptyList()) }

    LaunchedEffect(contactId) {
        viewModel.loadContactById(contactId)
    }

    val contact by viewModel.currentContact.collectAsState()

    LaunchedEffect(contact) {
        val c = contact
        if (c != null && c.numbers.isNotEmpty()) {
            contactCallLogs = viewModel.loadCallLogsForContact(c.numbers.map { it.number })
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    SalimBackButton(onClick = onBack)
                },
                actions = {
                    TextButton(
                        onClick = { onEditContact(contactId) },
                        modifier = Modifier.testTag("contact_edit_button")
                    ) {
                        Text(
                            text = "Edit",
                            color = SalimBlue,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val currentContact = contact
            if (currentContact == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SalimBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Contact Avatar (Clickable to change)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { showAvatarPicker = true }
                            .testTag("contact_avatar_picker_trigger"),
                        contentAlignment = Alignment.Center
                    ) {
                        SalimAvatar(
                            name = currentContact.name,
                            photoUri = currentContact.photoUri,
                            size = 100.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Change Photo",
                        style = MaterialTheme.typography.labelSmall.copy(color = SalimBlue, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .clickable { showAvatarPicker = true }
                            .padding(4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact Name
                    Text(
                        text = currentContact.name.ifBlank { "No Name" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (!currentContact.organization.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentContact.organization ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val cleanNumber = currentContact.primaryNumber.replace("[^0-9+]".toRegex(), "")

                    // Primary Action Buttons Row (Call, Message, Video Call)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionTile(
                            icon = Icons.Default.Call,
                            label = "Call",
                            enabled = currentContact.primaryNumber.isNotBlank(),
                            onClick = {
                                viewModel.makeCall(currentContact.primaryNumber)
                            }
                        )
                        ActionTile(
                            icon = Icons.Default.Message,
                            label = "Message",
                            enabled = currentContact.primaryNumber.isNotBlank(),
                            onClick = {
                                viewModel.sendSms(currentContact.primaryNumber)
                            }
                        )
                        ActionTile(
                            icon = Icons.Default.Videocam,
                            label = "Video",
                            enabled = currentContact.primaryNumber.isNotBlank(),
                            onClick = {
                                CallManager.startVideoCall(context, currentContact.primaryNumber)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Connected Apps Row (WhatsApp, Telegram, Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionTile(
                            icon = Icons.Default.Chat,
                            label = "WhatsApp",
                            tint = SalimGreen,
                            enabled = cleanNumber.isNotBlank(),
                            onClick = {
                                try {
                                    val waIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber")
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(waIntent)
                                } catch (_: Exception) {
                                    scope.launch { snackbarHostState.showSnackbar("Could not open WhatsApp") }
                                }
                            }
                        )
                        ActionTile(
                            icon = Icons.Default.Send,
                            label = "Telegram",
                            tint = SalimBlue,
                            enabled = cleanNumber.isNotBlank(),
                            onClick = {
                                try {
                                    val tgIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://t.me/+$cleanNumber")
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(tgIntent)
                                } catch (_: Exception) {
                                    scope.launch { snackbarHostState.showSnackbar("Could not open Telegram") }
                                }
                            }
                        )
                        ActionTile(
                            icon = Icons.Default.Share,
                            label = "Share",
                            enabled = true,
                            onClick = {
                                val shareText = buildString {
                                    appendLine(currentContact.name)
                                    currentContact.numbers.forEach { appendLine("${it.type}: ${it.number}") }
                                    currentContact.emails.forEach { appendLine("Email: $it") }
                                    if (!currentContact.organization.isNullOrBlank()) {
                                        appendLine("Company: ${currentContact.organization}")
                                    }
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(intent, "Share Contact"))
                                } catch (_: Exception) {}
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Phone numbers card
                    if (currentContact.numbers.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Phone Numbers",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                currentContact.numbers.forEachIndexed { index, phone ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.makeCall(phone.number) },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = phone.type,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = phone.number,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = SalimBlue,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(phone.number))
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Number copied to clipboard")
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Copy number",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Call History Dropdown Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { callHistoryExpanded = !callHistoryExpanded }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = SalimBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Call History",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    if (contactCallLogs.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${contactCallLogs.size})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (callHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (callHistoryExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(visible = callHistoryExpanded) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    if (contactCallLogs.isEmpty()) {
                                        Text(
                                            text = "No prior call records found with this contact.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    } else {
                                        contactCallLogs.take(10).forEachIndexed { idx, record ->
                                            if (idx > 0) {
                                                HorizontalDivider(
                                                    thickness = 0.5.dp,
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                    modifier = Modifier.padding(vertical = 6.dp)
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    val (callIcon, iconTint) = when (record.type) {
                                                        CallType.MISSED, CallType.REJECTED -> Icons.Default.CallMissed to SalimRed
                                                        CallType.OUTGOING -> Icons.Default.CallMade to SalimBlue
                                                        CallType.INCOMING -> Icons.Default.CallReceived to SalimGreen
                                                        CallType.BLOCKED -> Icons.Default.Block to SalimRed
                                                        CallType.VOICEMAIL -> Icons.Default.Call to SalimBlue
                                                    }
                                                    Icon(imageVector = callIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = when (record.type) {
                                                                CallType.MISSED -> "Missed Call"
                                                                CallType.OUTGOING -> "Outgoing"
                                                                CallType.INCOMING -> "Incoming"
                                                                CallType.REJECTED -> "Declined"
                                                                CallType.BLOCKED -> "Blocked"
                                                                CallType.VOICEMAIL -> "Voicemail"
                                                            },
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                                        )
                                                        val timeStr = remember(record.date) {
                                                            SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(record.date))
                                                        }
                                                        Text(
                                                            text = timeStr,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                if (record.durationSeconds > 0) {
                                                    Text(
                                                        text = record.formattedDuration,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Audio & Call Customization Card (Ringtone, Background, SIM)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            ContactPreferenceRow(
                                icon = Icons.Default.MusicNote,
                                iconTint = SalimBlue,
                                title = "Ringtone",
                                subtitle = selectedRingtone,
                                onClick = { showRingtoneDialog = true }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            ContactPreferenceRow(
                                icon = Icons.Default.Photo,
                                iconTint = Color(0xFFAF52DE),
                                title = "Call background",
                                subtitle = selectedBackground,
                                onClick = { showCallBackgroundDialog = true }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            ContactPreferenceRow(
                                icon = Icons.Default.SimCard,
                                iconTint = SalimGreen,
                                title = "Default calling SIM",
                                subtitle = selectedSim,
                                onClick = { showSimDialog = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Actions Card (Favorite, Share, Block, Delete)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            DetailActionRow(
                                icon = if (currentContact.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                label = if (currentContact.isFavorite) "Remove from Favorites" else "Add to Favorites",
                                iconColor = if (currentContact.isFavorite) SalimYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                                onClick = { viewModel.toggleFavorite(currentContact) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            DetailActionRow(
                                icon = Icons.Default.Block,
                                label = "Block Contact",
                                isDestructive = true,
                                onClick = { showBlockDialog = true }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            DetailActionRow(
                                icon = Icons.Default.Delete,
                                label = "Delete Contact",
                                isDestructive = true,
                                onClick = { showDeleteDialog = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // DIALOG: Ringtone Selector
    if (showRingtoneDialog) {
        val ringtones = listOf("Follow system", "Salim Bell", "Marimba", "Reflection", "Classic Bell", "Silk", "Strum")
        AlertDialog(
            onDismissRequest = { showRingtoneDialog = false },
            title = { Text("Select Ringtone", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    ringtones.forEach { tone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRingtone = tone
                                    showRingtoneDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tone,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedRingtone == tone) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedRingtone == tone) SalimBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRingtoneDialog = false }) { Text("Done", color = SalimBlue) }
            }
        )
    }

    // DIALOG: Call Background Selector
    if (showCallBackgroundDialog) {
        val bgs = listOf("Contact photo priority", "Custom Wallpaper", "Default Dark Glass")
        AlertDialog(
            onDismissRequest = { showCallBackgroundDialog = false },
            title = { Text("Call Background", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    bgs.forEach { bg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBackground = bg
                                    showCallBackgroundDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = bg,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedBackground == bg) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedBackground == bg) SalimBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCallBackgroundDialog = false }) { Text("Done", color = SalimBlue) }
            }
        )
    }

    // DIALOG: SIM Selector
    if (showSimDialog) {
        val sims = listOf("Follow system", "SIM 1", "SIM 2")
        AlertDialog(
            onDismissRequest = { showSimDialog = false },
            title = { Text("Default Calling SIM", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    sims.forEach { sim ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSim = sim
                                    showSimDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = sim,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedSim == sim) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSim == sim) SalimBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSimDialog = false }) { Text("Done", color = SalimBlue) }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        SalimConfirmationDialog(
            title = "Delete Contact",
            message = "Are you sure you want to delete ${contact?.name}? It will be moved to Recently Deleted.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                contact?.id?.let { id ->
                    viewModel.deleteContact(id, onDone = onBack)
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Block Confirmation Dialog
    if (showBlockDialog) {
        SalimConfirmationDialog(
            title = "Block Contact",
            message = "Calls and messages from ${contact?.name} will be blocked. You can unblock anytime from Settings.",
            confirmLabel = "Block",
            isDestructive = true,
            onConfirm = {
                contact?.let { c ->
                    viewModel.blockContact(c) {
                        scope.launch {
                            snackbarHostState.showSnackbar("${c.name} has been blocked")
                        }
                    }
                }
            },
            onDismiss = { showBlockDialog = false }
        )
    }

    // Avatar Picker Sheet
    if (showAvatarPicker && contact != null) {
        SalimAvatarPickerSheet(
            currentAvatarUri = contact?.photoUri,
            contactName = contact?.name ?: "",
            onAvatarSelected = { uri ->
                viewModel.updateAvatar(contactId, uri)
                showAvatarPicker = false
            },
            onDismiss = { showAvatarPicker = false }
        )
    }
}

@Composable
private fun ContactPreferenceRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Select $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    tint: Color = SalimBlue,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.4f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = if (enabled) 0.12f else 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint.copy(alpha = alpha),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
        )
    }
}

@Composable
private fun DetailActionRow(
    icon: ImageVector,
    label: String,
    iconColor: Color = SalimBlue,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val textColor = if (isDestructive) SalimRed else MaterialTheme.colorScheme.onBackground
    val finalIconColor = if (isDestructive) SalimRed else iconColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = finalIconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            fontWeight = if (isDestructive) FontWeight.Medium else FontWeight.Normal
        )
    }
}
