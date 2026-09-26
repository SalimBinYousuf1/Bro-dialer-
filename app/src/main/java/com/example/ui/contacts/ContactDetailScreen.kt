package com.example.ui.contacts

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.telephony.SubscriptionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.core.content.ContextCompat
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.domain.usecase.SocialAppAvailabilityHelper
import com.example.telephony.CallManager
import com.example.ui.components.SalimAvatar
import com.example.ui.components.SalimAvatarPickerSheet
import com.example.ui.components.SalimBackButton
import com.example.ui.components.SalimConfirmationDialog
import com.example.ui.theme.BrandColors
import com.example.ui.theme.BrandIcons
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostCard
import com.example.ui.theme.FrostIconButton
import com.example.ui.theme.FrostInteractiveCard
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.GlassBackgroundLight
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass
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
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    // Dialogs for preferences
    var showCallBackgroundDialog by remember { mutableStateOf(false) }
    var showSimDialog by remember { mutableStateOf(false) }

    // Custom Contact Settings state observed from repository
    val contactCustomization by viewModel.getContactCustomization(contactId).collectAsState(initial = null)

    // Call history dropdown
    var callHistoryExpanded by remember { mutableStateOf(false) }
    var contactCallLogs by remember { mutableStateOf<List<CallRecord>>(emptyList()) }

    LaunchedEffect(contactId) {
        viewModel.loadContactById(contactId)
    }

    val contact by viewModel.currentContact.collectAsState()

    // Native Ringtone Picker Launcher
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            val title = if (uri != null) {
                try {
                    RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: "Custom Ringtone"
                } catch (_: Exception) {
                    "Custom Ringtone"
                }
            } else "Silent"
            viewModel.saveContactRingtone(contactId, uri?.toString(), title)
            scope.launch { snackbarHostState.showSnackbar("Ringtone updated: $title") }
        }
    }

    // Photo Picker for Call Background
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.saveContactCallBackground(contactId, uri)
            scope.launch { snackbarHostState.showSnackbar("Call background photo saved") }
        }
    }

    LaunchedEffect(contact) {
        val c = contact
        if (c != null && c.numbers.isNotEmpty()) {
            contactCallLogs = viewModel.loadCallLogsForContact(c.numbers.map { it.number })
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    SalimBackButton(onClick = onBack)
                },
                actions = {
                    FrostButton(
                        text = "Edit",
                        onClick = { onEditContact(contactId) },
                        testTag = "contact_edit_button"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
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
                    CircularProgressIndicator(color = textPrimary)
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
                            .size(104.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))
                    FrostButton(
                        text = if (currentContact.photoUri != null) "Edit Photo" else "Add Photo",
                        onClick = { showAvatarPicker = true },
                        testTag = "change_photo_button"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contact Name
                    Text(
                        text = currentContact.name.ifBlank { "No Name" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = textPrimary
                    )
                    if (!currentContact.organization.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentContact.organization ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

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
                                CallManager.startVideoCall(context, currentContact.primaryNumber, currentContact.name, currentContact.photoUri)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val hasWhatsApp = remember(currentContact, context) {
                        SocialAppAvailabilityHelper.hasWhatsAppAvailable(context, currentContact.id, currentContact.primaryNumber)
                    }
                    val hasTelegram = remember(currentContact, context) {
                        SocialAppAvailabilityHelper.hasTelegramAvailable(context, currentContact.id, currentContact.primaryNumber)
                    }

                    // Connected Apps Row (WhatsApp, Telegram, Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (hasWhatsApp) {
                            ActionTile(
                                icon = BrandIcons.WhatsApp,
                                label = "WhatsApp",
                                tint = BrandColors.WhatsApp,
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
                        }
                        if (hasTelegram) {
                            ActionTile(
                                icon = BrandIcons.Telegram,
                                label = "Telegram",
                                tint = BrandColors.Telegram,
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
                        }
                        ActionTile(
                            icon = Icons.Default.Share,
                            label = "Share",
                            enabled = true,
                            onClick = {
                                val shareText = buildString {
                                    appendLine(currentContact.name)
                                    currentContact.numbers.forEach { appendLine("${it.type}: ${it.number}") }
                                    if (currentContact.emails.isNotEmpty()) {
                                        currentContact.emails.forEach { appendLine("Email: $it") }
                                    }
                                    if (!currentContact.organization.isNullOrBlank()) {
                                        appendLine("Company: ${currentContact.organization}")
                                    }
                                }.trim()
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, currentContact.name)
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
                        FrostCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Phone Numbers",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textMuted
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                currentContact.numbers.forEachIndexed { index, phone ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            thickness = 0.5.dp,
                                            color = textMuted.copy(alpha = 0.2f)
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
                                                color = textMuted
                                            )
                                            Text(
                                                text = phone.number,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = textPrimary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                        FrostIconButton(
                                            icon = Icons.Default.ContentCopy,
                                            contentDescription = "Copy number",
                                            size = 36.dp,
                                            iconSize = 16.dp,
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(phone.number))
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Number copied to clipboard")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Call History Dropdown Card
                    FrostCard(modifier = Modifier.fillMaxWidth()) {
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
                                        tint = textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Call History",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = textPrimary
                                    )
                                    if (contactCallLogs.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${contactCallLogs.size})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textMuted
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (callHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (callHistoryExpanded) "Collapse" else "Expand",
                                    tint = textMuted
                                )
                            }

                            AnimatedVisibility(visible = callHistoryExpanded) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Column {
                                        if (contactCallLogs.isEmpty()) {
                                            Text(
                                                text = "No prior call records found with this contact.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = textMuted,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        } else {
                                            contactCallLogs.forEachIndexed { idx, record ->
                                                if (idx > 0) {
                                                    HorizontalDivider(
                                                        thickness = 0.5.dp,
                                                        color = textMuted.copy(alpha = 0.2f),
                                                        modifier = Modifier.padding(vertical = 6.dp)
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        val callIcon = when (record.type) {
                                                            CallType.MISSED, CallType.REJECTED -> Icons.Default.CallMissed
                                                            CallType.OUTGOING -> Icons.Default.CallMade
                                                            CallType.INCOMING -> Icons.Default.CallReceived
                                                            CallType.BLOCKED -> Icons.Default.Block
                                                            CallType.VOICEMAIL -> Icons.Default.Call
                                                        }
                                                        val isMissed = record.type == CallType.MISSED || record.type == CallType.REJECTED
                                                        val callTint = if (isMissed) BrandColors.AppleRed else textPrimary
                                                        Icon(imageVector = callIcon, contentDescription = null, tint = callTint, modifier = Modifier.size(16.dp))
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
                                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                                color = if (isMissed) BrandColors.AppleRed else textPrimary
                                                            )
                                                            val timeStr = remember(record.date) {
                                                                SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(record.date))
                                                            }
                                                            Text(
                                                                text = timeStr,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = textMuted
                                                            )
                                                        }
                                                    }
                                                    if (record.durationSeconds > 0) {
                                                        Text(
                                                            text = record.formattedDuration,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = textMuted
                                                        )
                                                    }
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
                    FrostCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            val ringtoneTitle = contactCustomization?.ringtoneTitle ?: "Follow system"
                            ContactPreferenceRow(
                                icon = Icons.Default.MusicNote,
                                title = "Ringtone",
                                subtitle = ringtoneTitle,
                                onClick = {
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone for ${currentContact.name}")
                                        contactCustomization?.ringtoneUri?.let {
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(it))
                                        }
                                    }
                                    try {
                                        ringtonePickerLauncher.launch(intent)
                                    } catch (_: Exception) {
                                        scope.launch { snackbarHostState.showSnackbar("Could not open system ringtone picker") }
                                    }
                                }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = textMuted.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            val bgTitle = if (contactCustomization?.callBackgroundUri != null) "Custom Wallpaper" else "Default Frost Glass"
                            ContactPreferenceRow(
                                icon = Icons.Default.Photo,
                                title = "Call background",
                                subtitle = bgTitle,
                                onClick = { showCallBackgroundDialog = true }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = textMuted.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            val simTitle = when (val simId = contactCustomization?.defaultSimId ?: -1) {
                                -1 -> "Follow system"
                                1 -> "SIM 1"
                                2 -> "SIM 2"
                                else -> "SIM $simId"
                            }
                            ContactPreferenceRow(
                                icon = Icons.Default.SimCard,
                                title = "Default calling SIM",
                                subtitle = simTitle,
                                onClick = { showSimDialog = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Actions Card (Favorite, Share, Block, Delete)
                    FrostCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            DetailActionRow(
                                icon = if (currentContact.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                label = if (currentContact.isFavorite) "Remove from Favorites" else "Add to Favorites",
                                onClick = { viewModel.toggleFavorite(currentContact) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = textMuted.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            DetailActionRow(
                                icon = Icons.Default.Block,
                                label = "Block Contact",
                                onClick = { showBlockDialog = true }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = textMuted.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            DetailActionRow(
                                icon = Icons.Default.Delete,
                                label = "Delete Contact",
                                onClick = { showDeleteDialog = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // DIALOG: Call Background Selector (Gallery, Avatar, Reset)
    if (showCallBackgroundDialog) {
        val bgOptions = listOf(
            "Choose Photo from Gallery",
            "Use Contact Avatar",
            "Reset to Default Frost Glass"
        )
        AlertDialog(
            onDismissRequest = { showCallBackgroundDialog = false },
            title = { Text("Call Background", fontWeight = FontWeight.Bold, color = textPrimary) },
            text = {
                Column {
                    bgOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showCallBackgroundDialog = false
                                    when (opt) {
                                        "Choose Photo from Gallery" -> {
                                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        }
                                        "Use Contact Avatar" -> {
                                            contact?.photoUri?.let { uriStr ->
                                                viewModel.saveContactCallBackground(contactId, Uri.parse(uriStr))
                                            }
                                        }
                                        "Reset to Default Frost Glass" -> {
                                            viewModel.saveContactCallBackground(contactId, null)
                                        }
                                    }
                                }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                FrostButton(
                    text = "Close",
                    onClick = { showCallBackgroundDialog = false }
                )
            },
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        )
    }

    // DIALOG: Default Calling SIM Selector
    if (showSimDialog) {
        val subscriptionManager = remember {
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
        }
        val subList = remember(context) {
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    subscriptionManager?.activeSubscriptionInfoList ?: emptyList()
                } else emptyList()
            } catch (_: Exception) { emptyList() }
        }

        val currentSimId = contactCustomization?.defaultSimId ?: -1

        AlertDialog(
            onDismissRequest = { showSimDialog = false },
            title = { Text("Default Calling SIM", fontWeight = FontWeight.Bold, color = textPrimary) },
            text = {
                Column {
                    // Option: Follow System
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.saveContactDefaultSim(contactId, -1)
                                showSimDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Follow System (Ask / Default)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (currentSimId == -1) FontWeight.Bold else FontWeight.Normal,
                            color = textPrimary
                        )
                        if (currentSimId == -1) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (subList.isNotEmpty()) {
                        subList.forEach { sub ->
                            val simId = sub.simSlotIndex + 1
                            val isSelected = currentSimId == simId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.saveContactDefaultSim(contactId, simId)
                                        showSimDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${sub.displayName} (SIM $simId)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textPrimary
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard SIM 1 & SIM 2 fallback
                        listOf(1 to "SIM 1", 2 to "SIM 2").forEach { (simId, label) ->
                            val isSelected = currentSimId == simId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.saveContactDefaultSim(contactId, simId)
                                        showSimDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textPrimary
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                FrostButton(
                    text = "Done",
                    onClick = { showSimDialog = false }
                )
            },
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
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
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = textPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Select $title",
            tint = textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    tint: Color? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        FrostIconButton(
            icon = icon,
            contentDescription = label,
            onClick = onClick,
            enabled = enabled,
            tint = tint,
            size = 52.dp,
            iconSize = 24.dp,
            elevation = 2.dp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = if (enabled) textPrimary else textMuted
        )
    }
}

@Composable
private fun DetailActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight

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
            tint = textPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = textPrimary,
            fontWeight = FontWeight.Normal
        )
    }
}
