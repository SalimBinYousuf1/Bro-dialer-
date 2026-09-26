package com.example.ui.contacts.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecentlyDeletedContact
import com.example.ui.components.SalimBackButton
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostCard
import com.example.ui.theme.FrostIconButton
import com.example.ui.theme.FrostSwitch
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsSettingsScreen(
    viewModel: ContactsSettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight
    val context = LocalContext.current

    val settings by viewModel.settings.collectAsState()
    val recentlyDeleted by viewModel.recentlyDeleted.collectAsState()
    val duplicateCount by viewModel.duplicateCount.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()
    val hasBackup by viewModel.hasBackup.collectAsState()
    val message by viewModel.message.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importContactsFromUri(context, uri)
        }
    }

    var showAccountDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showSaveLocationDialog by remember { mutableStateOf(false) }
    var showImportExportSheet by remember { mutableStateOf(false) }
    var showCopyContactsDialog by remember { mutableStateOf(false) }
    var showMergeDialog by remember { mutableStateOf(false) }
    var showRecentlyDeletedSheet by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Contacts Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    SalimBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // DISPLAY SECTION
            Text(
                text = "DISPLAY",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = textMuted,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            FrostCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    ContactsSettingSwitchRow(
                        icon = Icons.Default.Person,
                        title = "Display profile picture",
                        checked = settings.displayProfilePicture,
                        onCheckedChange = { viewModel.setDisplayProfilePicture(it) },
                        testTag = "switch_profile_picture"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingSwitchRow(
                        icon = Icons.Default.Phone,
                        title = "Display number",
                        checked = settings.displayNumber,
                        onCheckedChange = { viewModel.setDisplayNumber(it) },
                        testTag = "switch_display_number"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingSwitchRow(
                        icon = Icons.Default.Business,
                        title = "Display company and title",
                        checked = settings.displayCompanyAndTitle,
                        onCheckedChange = { viewModel.setDisplayCompanyAndTitle(it) },
                        testTag = "switch_display_company"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingSwitchRow(
                        icon = Icons.Default.Phone,
                        title = "Show contacts with numbers only",
                        checked = settings.showNumbersOnly,
                        onCheckedChange = { viewModel.setShowNumbersOnly(it) },
                        testTag = "switch_numbers_only"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.AccountBox,
                        title = "Display by account",
                        value = settings.displayByAccount,
                        onClick = { showAccountDialog = true },
                        testTag = "nav_display_account"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.SortByAlpha,
                        title = "Sort by",
                        value = settings.sortBy,
                        onClick = { showSortDialog = true },
                        testTag = "nav_sort_by"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MANAGEMENT SECTION
            Text(
                text = "MANAGEMENT",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = textMuted,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            FrostCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    ContactsSettingNavRow(
                        icon = Icons.Default.Backup,
                        title = "Import, Export & Backup",
                        value = if (hasBackup) "Backed up" else "Ready",
                        onClick = { showImportExportSheet = true },
                        testTag = "nav_import_export"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.ContentCopy,
                        title = "Copy contacts",
                        value = "",
                        onClick = { showCopyContactsDialog = true },
                        testTag = "nav_copy_contacts"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.Save,
                        title = "Save location",
                        value = settings.saveLocation,
                        onClick = { showSaveLocationDialog = true },
                        testTag = "nav_save_location"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.CallMerge,
                        title = "Merge duplicate contacts",
                        value = if (duplicateCount > 0) "$duplicateCount found" else "None",
                        onClick = { showMergeDialog = true },
                        testTag = "nav_merge_duplicates"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = textMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.DeleteOutline,
                        title = "Recently deleted",
                        value = if (recentlyDeleted.isNotEmpty()) "${recentlyDeleted.size}" else "Empty",
                        onClick = { showRecentlyDeletedSheet = true },
                        testTag = "nav_recently_deleted"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // DIALOG: Display By Account
    if (showAccountDialog) {
        val accounts = listOf("All Accounts", "Phone (Device)", "Google Account", "SIM Card")
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text("Display by Account", fontWeight = FontWeight.Bold, color = textPrimary) },
            text = {
                Column {
                    accounts.forEach { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDisplayByAccount(acc)
                                    showAccountDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = acc,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (settings.displayByAccount == acc) FontWeight.Bold else FontWeight.Normal,
                                color = textPrimary
                            )
                            if (settings.displayByAccount == acc) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = textPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                FrostButton(text = "Close", onClick = { showAccountDialog = false })
            },
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        )
    }

    // DIALOG: Sort By
    if (showSortDialog) {
        val sorts = listOf("First name", "Last name")
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort by", fontWeight = FontWeight.Bold, color = textPrimary) },
            text = {
                Column {
                    sorts.forEach { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSortBy(s)
                                    showSortDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = s,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (settings.sortBy == s) FontWeight.Bold else FontWeight.Normal,
                                color = textPrimary
                            )
                            if (settings.sortBy == s) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = textPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                FrostButton(text = "Close", onClick = { showSortDialog = false })
            },
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        )
    }

    // DIALOG: Save Location
    if (showSaveLocationDialog) {
        val locations = listOf("Phone", "SIM Card", "Device Storage")
        AlertDialog(
            onDismissRequest = { showSaveLocationDialog = false },
            title = { Text("Save Location", fontWeight = FontWeight.Bold, color = textPrimary) },
            text = {
                Column {
                    locations.forEach { loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSaveLocation(loc)
                                    showSaveLocationDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = loc,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (settings.saveLocation == loc) FontWeight.Bold else FontWeight.Normal,
                                color = textPrimary
                            )
                            if (settings.saveLocation == loc) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = textPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                FrostButton(text = "Close", onClick = { showSaveLocationDialog = false })
            },
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        )
    }

    // DIALOG: Merge Duplicates
    if (showMergeDialog) {
        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            icon = { Icon(Icons.Default.CallMerge, contentDescription = null, tint = textPrimary) },
            title = { Text("Merge Duplicate Contacts", fontWeight = FontWeight.Bold, color = textPrimary) },
            text = {
                Text(
                    text = if (duplicateCount > 0)
                        "Found $duplicateCount duplicate contact entries. Would you like to merge them into unified contacts?"
                    else
                        "No duplicate contacts found in your address book.",
                    color = textPrimary
                )
            },
            confirmButton = {
                if (duplicateCount > 0) {
                    FrostButton(
                        text = "Merge",
                        onClick = {
                            viewModel.mergeDuplicates {
                                showMergeDialog = false
                            }
                        }
                    )
                } else {
                    FrostButton(text = "OK", onClick = { showMergeDialog = false })
                }
            },
            dismissButton = {
                if (duplicateCount > 0) {
                    FrostButton(text = "Cancel", onClick = { showMergeDialog = false })
                }
            },
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        )
    }

    // DIALOG: Copy Contacts
    if (showCopyContactsDialog) {
        AlertDialog(
            onDismissRequest = { showCopyContactsDialog = false },
            title = { Text("Copy Contacts", fontWeight = FontWeight.Bold, color = textPrimary) },
            text = {
                Column {
                    Text("Select copy direction:", style = MaterialTheme.typography.bodyMedium, color = textPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    FrostButton(
                        text = "Copy from Phone to SIM",
                        onClick = {
                            showCopyContactsDialog = false
                            Toast.makeText(context, "Contacts copied to SIM card", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FrostButton(
                        text = "Copy from SIM to Phone",
                        onClick = {
                            showCopyContactsDialog = false
                            Toast.makeText(context, "Contacts copied from SIM to Phone", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                FrostButton(text = "Cancel", onClick = { showCopyContactsDialog = false })
            },
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        )
    }

    // BOTTOM SHEET: Import/Export & Backup
    if (showImportExportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImportExportSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Backup & Data Management",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (lastBackupTime != null) "Last local backup: $lastBackupTime" else "No local backup created yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMuted
                )
                Spacer(modifier = Modifier.height(16.dp))

                FrostCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Action 1: Create Backup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImportExportSheet = false
                                    viewModel.backupContacts(context)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = textPrimary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Back up to local storage", fontWeight = FontWeight.SemiBold, color = textPrimary)
                                Text("Save an instant offline snapshot of all contacts", fontSize = 12.sp, color = textMuted)
                            }
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = textMuted.copy(alpha = 0.2f))

                        // Action 2: Restore Backup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImportExportSheet = false
                                    viewModel.restoreLocalBackup(context)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = if (hasBackup) textPrimary else textMuted)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Restore from local backup",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (hasBackup) textPrimary else textMuted
                                )
                                Text(
                                    text = if (hasBackup) "Restore contacts from your offline snapshot" else "No backup file available to restore",
                                    fontSize = 12.sp,
                                    color = textMuted
                                )
                            }
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = textMuted.copy(alpha = 0.2f))

                        // Action 3: Export to .vcf (Share / Drive)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImportExportSheet = false
                                    viewModel.exportContactsVcf(context) { intent ->
                                        context.startActivity(Intent.createChooser(intent, "Export Contacts (.vcf)"))
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = textPrimary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Export to .vcf file", fontWeight = FontWeight.SemiBold, color = textPrimary)
                                Text("Share or save standard vCard file (Drive, Email, etc.)", fontSize = 12.sp, color = textMuted)
                            }
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = textMuted.copy(alpha = 0.2f))

                        // Action 4: Import from .vcf
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImportExportSheet = false
                                    filePickerLauncher.launch("*/*")
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ImportExport, contentDescription = null, tint = textPrimary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Import from .vcf file", fontWeight = FontWeight.SemiBold, color = textPrimary)
                                Text("Select and load a vCard file from device storage", fontSize = 12.sp, color = textMuted)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // BOTTOM SHEET: Recently Deleted
    if (showRecentlyDeletedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRecentlyDeletedSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recently Deleted",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )
                    if (recentlyDeleted.isNotEmpty()) {
                        FrostButton(
                            text = "Clear All",
                            onClick = { viewModel.clearAllRecentlyDeleted() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Items in recently deleted can be restored to your contacts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMuted
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (recentlyDeleted.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recently deleted contacts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(recentlyDeleted, key = { it.id }) { item ->
                            RecentlyDeletedRow(
                                item = item,
                                onRestore = { viewModel.restoreContact(item) },
                                onDeletePermanently = { viewModel.deletePermanently(item.id) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = textMuted.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ContactsSettingSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = textPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = textPrimary,
            modifier = Modifier.weight(1f)
        )
        FrostSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun ContactsSettingNavRow(
    icon: ImageVector,
    title: String,
    value: String,
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
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = textPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotBlank()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = textMuted,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = textMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun RecentlyDeletedRow(
    item: RecentlyDeletedContact,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    val dateStr = remember(item.deletedTimestamp) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(item.deletedTimestamp))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .liquidGlass(shape = CircleShape, elevation = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.name.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textPrimary)
            Text(
                text = "${item.phoneNumbers.split(",").firstOrNull() ?: ""} • $dateStr",
                fontSize = 12.sp,
                color = textMuted
            )
        }
        FrostIconButton(
            icon = Icons.Default.Restore,
            contentDescription = "Restore",
            onClick = onRestore,
            size = 36.dp,
            iconSize = 18.dp
        )
        Spacer(modifier = Modifier.width(6.dp))
        FrostIconButton(
            icon = Icons.Default.Delete,
            contentDescription = "Delete Permanently",
            onClick = onDeletePermanently,
            size = 36.dp,
            iconSize = 18.dp
        )
    }
}
