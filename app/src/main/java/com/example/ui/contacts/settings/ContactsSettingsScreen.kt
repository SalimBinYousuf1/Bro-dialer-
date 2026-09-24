package com.example.ui.contacts.settings

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecentlyDeletedContact
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimRed
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
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val recentlyDeleted by viewModel.recentlyDeleted.collectAsState()
    val duplicateCount by viewModel.duplicateCount.collectAsState()
    val message by viewModel.message.collectAsState()

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
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("contacts_settings_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = SalimBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    ContactsSettingSwitchRow(
                        icon = Icons.Default.Person,
                        iconTint = SalimBlue,
                        title = "Display profile picture",
                        checked = settings.displayProfilePicture,
                        onCheckedChange = { viewModel.setDisplayProfilePicture(it) },
                        testTag = "switch_profile_picture"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingSwitchRow(
                        icon = Icons.Default.Phone,
                        iconTint = SalimGreen,
                        title = "Display number",
                        checked = settings.displayNumber,
                        onCheckedChange = { viewModel.setDisplayNumber(it) },
                        testTag = "switch_display_number"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingSwitchRow(
                        icon = Icons.Default.Business,
                        iconTint = Color(0xFF5856D6),
                        title = "Display company and title",
                        checked = settings.displayCompanyAndTitle,
                        onCheckedChange = { viewModel.setDisplayCompanyAndTitle(it) },
                        testTag = "switch_display_company"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingSwitchRow(
                        icon = Icons.Default.Phone,
                        iconTint = Color(0xFFFF9500),
                        title = "Show contacts with numbers only",
                        checked = settings.showNumbersOnly,
                        onCheckedChange = { viewModel.setShowNumbersOnly(it) },
                        testTag = "switch_numbers_only"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.AccountBox,
                        iconTint = SalimBlue,
                        title = "Display by account",
                        value = settings.displayByAccount,
                        onClick = { showAccountDialog = true },
                        testTag = "nav_display_account"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.SortByAlpha,
                        iconTint = Color(0xFFAF52DE),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    ContactsSettingNavRow(
                        icon = Icons.Default.ImportExport,
                        iconTint = SalimBlue,
                        title = "Import/Export",
                        value = "",
                        onClick = { showImportExportSheet = true },
                        testTag = "nav_import_export"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.ContentCopy,
                        iconTint = Color(0xFF5856D6),
                        title = "Copy contacts",
                        value = "",
                        onClick = { showCopyContactsDialog = true },
                        testTag = "nav_copy_contacts"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.Save,
                        iconTint = SalimGreen,
                        title = "Save location",
                        value = settings.saveLocation,
                        onClick = { showSaveLocationDialog = true },
                        testTag = "nav_save_location"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.CallMerge,
                        iconTint = Color(0xFFFF9500),
                        title = "Merge duplicate contacts",
                        value = if (duplicateCount > 0) "$duplicateCount found" else "None",
                        onClick = { showMergeDialog = true },
                        testTag = "nav_merge_duplicates"
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ContactsSettingNavRow(
                        icon = Icons.Default.DeleteOutline,
                        iconTint = SalimRed,
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
            title = { Text("Display by Account", fontWeight = FontWeight.Bold) },
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
                                color = if (settings.displayByAccount == acc) SalimBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountDialog = false }) { Text("Close", color = SalimBlue) }
            }
        )
    }

    // DIALOG: Sort By
    if (showSortDialog) {
        val sorts = listOf("First name", "Last name")
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort by", fontWeight = FontWeight.Bold) },
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
                                color = if (settings.sortBy == s) SalimBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) { Text("Close", color = SalimBlue) }
            }
        )
    }

    // DIALOG: Save Location
    if (showSaveLocationDialog) {
        val locations = listOf("Phone", "SIM Card", "Device Storage")
        AlertDialog(
            onDismissRequest = { showSaveLocationDialog = false },
            title = { Text("Save Location", fontWeight = FontWeight.Bold) },
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
                                color = if (settings.saveLocation == loc) SalimBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSaveLocationDialog = false }) { Text("Close", color = SalimBlue) }
            }
        )
    }

    // DIALOG: Merge Duplicates
    if (showMergeDialog) {
        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            icon = { Icon(Icons.Default.CallMerge, contentDescription = null, tint = Color(0xFFFF9500)) },
            title = { Text("Merge Duplicate Contacts", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = if (duplicateCount > 0)
                        "Found $duplicateCount duplicate contact entries. Would you like to merge them into unified contacts?"
                    else
                        "No duplicate contacts found in your address book."
                )
            },
            confirmButton = {
                if (duplicateCount > 0) {
                    TextButton(onClick = {
                        viewModel.mergeDuplicates {
                            showMergeDialog = false
                        }
                    }) {
                        Text("Merge", color = SalimBlue, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showMergeDialog = false }) {
                    Text(if (duplicateCount > 0) "Cancel" else "OK", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // DIALOG: Copy Contacts
    if (showCopyContactsDialog) {
        AlertDialog(
            onDismissRequest = { showCopyContactsDialog = false },
            title = { Text("Copy Contacts", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select copy direction:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            showCopyContactsDialog = false
                            Toast.makeText(context, "Contacts copied to SIM card", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy from Phone to SIM", color = SalimBlue)
                    }
                    TextButton(
                        onClick = {
                            showCopyContactsDialog = false
                            Toast.makeText(context, "Contacts copied from SIM to Phone", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy from SIM to Phone", color = SalimBlue)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCopyContactsDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    // BOTTOM SHEET: Import/Export
    if (showImportExportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImportExportSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Import / Export Contacts",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImportExportSheet = false
                                    Toast.makeText(context, "Exported contacts to vCard (.vcf)", Toast.LENGTH_LONG).show()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = SalimBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Export to .vcf file", fontWeight = FontWeight.SemiBold)
                                Text("Save contacts to storage as standard vCard file", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImportExportSheet = false
                                    Toast.makeText(context, "Select a .vcf file to import", Toast.LENGTH_SHORT).show()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ImportExport, contentDescription = null, tint = SalimGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Import from .vcf file", fontWeight = FontWeight.SemiBold)
                                Text("Restore contacts from vCard file in storage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            containerColor = MaterialTheme.colorScheme.background
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
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    if (recentlyDeleted.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAllRecentlyDeleted() }) {
                            Text("Clear All", color = SalimRed)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Items in recently deleted can be restored to your contacts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
    iconTint: Color,
    title: String,
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
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SalimBlue),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun ContactsSettingNavRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotBlank()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun RecentlyDeletedRow(
    item: RecentlyDeletedContact,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
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
                .clip(CircleShape)
                .background(Color(0xFFE5E5EA)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.name.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF636366)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                text = "${item.phoneNumbers.split(",").firstOrNull() ?: ""} • $dateStr",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRestore, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Restore, contentDescription = "Restore", tint = SalimBlue)
        }
        IconButton(onClick = onDeletePermanently, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Permanently", tint = SalimRed)
        }
    }
}
