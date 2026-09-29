package com.example.ui.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.domain.usecase.PhoneNumberHelper
import com.example.ui.components.SalimAvatar
import com.example.ui.components.SalimConfirmationDialog
import com.example.ui.components.SalimEmptyState
import com.example.ui.components.SalimSearchBar
import com.example.ui.theme.BrandColors
import com.example.ui.theme.BrandIcons
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostIconButton
import com.example.ui.theme.FrostInteractiveCard
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.GlassBackgroundLight
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.LocalDarkTheme
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.liquidGlassInteractive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    onContactClick: (Long) -> Unit,
    onAddContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = LocalDarkTheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val contacts by viewModel.filteredContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var activeContactForLongHoldSheet by remember { mutableStateOf<ContactItem?>(null) }

    // Group contacts cleanly: A..Z first, '#' at the very end. Never '0' or digits as section headers!
    val groupedContacts = remember(contacts) {
        contacts.groupBy {
            val trimmed = it.name.trim()
            val first = trimmed.firstOrNull()?.uppercaseChar() ?: '#'
            if (first in 'A'..'Z') first else '#'
        }.toSortedMap(compareBy { char ->
            if (char == '#') 'Z' + 1 else char
        })
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(paddingValues)
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar (Selection Mode actions or Normal Contacts header)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSelectionMode) "Selected (${selectedIds.size})" else "Contacts",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelectionMode) {
                            // Select All / Deselect All Icon Button
                            val allSelected = contacts.isNotEmpty() && contacts.all { selectedIds.contains(it.id) }
                            FrostIconButton(
                                icon = if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                                contentDescription = if (allSelected) "Deselect All" else "Select All",
                                onClick = {
                                    if (allSelected) viewModel.deselectAll() else viewModel.selectAll()
                                },
                                size = 40.dp,
                                iconSize = 20.dp,
                                testTag = "contacts_select_all_toggle"
                            )

                            // Delete Selected Icon Button
                            FrostIconButton(
                                icon = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                enabled = selectedIds.isNotEmpty(),
                                tint = if (selectedIds.isNotEmpty()) BrandColors.AppleRed else textMuted.copy(alpha = 0.4f),
                                onClick = { showDeleteConfirmDialog = true },
                                size = 40.dp,
                                iconSize = 20.dp,
                                testTag = "contacts_delete_selected"
                            )

                            // Cancel Selection Icon Button
                            FrostIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Cancel Selection",
                                onClick = { viewModel.setSelectionMode(false) },
                                size = 40.dp,
                                iconSize = 20.dp,
                                testTag = "contacts_cancel_selection"
                            )
                        } else {
                            if (contacts.isNotEmpty()) {
                                // Enter Selection Mode Button
                                FrostIconButton(
                                    icon = Icons.Default.Checklist,
                                    contentDescription = "Select Contacts",
                                    onClick = { viewModel.setSelectionMode(true) },
                                    size = 40.dp,
                                    iconSize = 20.dp,
                                    testTag = "contacts_enter_selection"
                                )
                            }

                            // Add Contact Button
                            FrostIconButton(
                                icon = Icons.Default.Add,
                                contentDescription = "Add Contact",
                                onClick = onAddContactClick,
                                size = 40.dp,
                                iconSize = 20.dp,
                                testTag = "add_contact_button"
                            )
                        }
                    }
                }

                // Search Bar
                SalimSearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    placeholder = "Search contacts, phone numbers...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )

                // Content List
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = textPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else if (contacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        SalimEmptyState(
                            icon = Icons.Default.Person,
                            title = if (searchQuery.isEmpty()) "No Contacts Yet" else "No Matches",
                            description = if (searchQuery.isEmpty()) {
                                "Add a new contact or grant Contacts permission to get started."
                            } else {
                                "No contacts matched \"$searchQuery\"."
                            },
                            actionLabel = if (searchQuery.isEmpty()) "Create Contact" else null,
                            onActionClick = if (searchQuery.isEmpty()) onAddContactClick else null
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        groupedContacts.forEach { (char, contactList) ->
                            item(key = "section_$char") {
                                Text(
                                    text = char.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = textMuted,
                                    modifier = Modifier.padding(start = 6.dp, top = 10.dp, bottom = 4.dp)
                                )
                            }

                            items(contactList, key = { it.id }) { contact ->
                                val isSelected = selectedIds.contains(contact.id)
                                ContactGlassRow(
                                    contact = contact,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(contact.id)
                                        } else {
                                            onContactClick(contact.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(contact.id)
                                        } else {
                                            activeContactForLongHoldSheet = contact
                                        }
                                    },
                                    onCallClick = {
                                        if (contact.primaryNumber.isNotBlank()) {
                                            viewModel.makeCall(contact.primaryNumber)
                                        } else {
                                            onContactClick(contact.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Bulk Delete
    if (showDeleteConfirmDialog) {
        SalimConfirmationDialog(
            title = "Delete Selected Contacts",
            message = "Are you sure you want to delete ${selectedIds.size} selected contact(s)?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteSelected {
                    scope.launch { snackbarHostState.showSnackbar("Deleted selected contacts") }
                }
                showDeleteConfirmDialog = false
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    // LONG-PRESS CONTEXT SHEET FOR CONTACTS (Apple iOS Style)
    activeContactForLongHoldSheet?.let { contact ->
        ModalBottomSheet(
            onDismissRequest = { activeContactForLongHoldSheet = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SalimAvatar(
                    name = contact.name,
                    photoUri = contact.photoUri,
                    size = 68.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
                if (contact.primaryNumber.isNotBlank()) {
                    Text(
                        text = PhoneNumberHelper.formatForDisplay(contact.primaryNumber),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textMuted
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action 1: Call
                if (contact.primaryNumber.isNotBlank()) {
                    FrostButton(
                        text = "Call ${PhoneNumberHelper.formatForDisplay(contact.primaryNumber)}",
                        icon = Icons.Default.Call,
                        onClick = {
                            val num = contact.primaryNumber
                            activeContactForLongHoldSheet = null
                            viewModel.makeCall(num)
                        },
                        isProminent = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Action 2: Send Message
                    FrostButton(
                        text = "Send Message",
                        icon = Icons.Default.Chat,
                        onClick = {
                            val num = contact.primaryNumber
                            activeContactForLongHoldSheet = null
                            viewModel.sendSms(num)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Action 3: WhatsApp
                    val cleanNumber = PhoneNumberHelper.normalizeNumber(contact.primaryNumber)
                    FrostButton(
                        text = "Message on WhatsApp",
                        onClick = {
                            activeContactForLongHoldSheet = null
                            try {
                                val waIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(waIntent)
                            } catch (_: Exception) {
                                scope.launch { snackbarHostState.showSnackbar("Could not open WhatsApp") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Action 4: Select Contact (Enter selection mode)
                FrostButton(
                    text = "Select Contact",
                    icon = Icons.Default.Checklist,
                    onClick = {
                        activeContactForLongHoldSheet = null
                        viewModel.setSelectionMode(true)
                        viewModel.toggleSelection(contact.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action 5: Share Contact
                FrostButton(
                    text = "Share Contact",
                    icon = Icons.Default.Share,
                    onClick = {
                        activeContactForLongHoldSheet = null
                        val shareText = "${contact.name}\n${contact.primaryNumber}".trim()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, contact.name)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(Intent.createChooser(shareIntent, "Share Contact"))
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactGlassRow(
    contact: ContactItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onCallClick: () -> Unit
) {
    val dark = LocalDarkTheme.current
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelectionMode) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) BrandColors.AppleRed else textPrimary,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onClick)
                    .padding(end = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(shape)
                .liquidGlass(
                    shape = shape,
                    elevation = 2.dp,
                    isElevated = isSelected
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SalimAvatar(
                    name = contact.name,
                    photoUri = contact.photoUri,
                    size = 46.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = textPrimary,
                            maxLines = 1
                        )
                        if (contact.isFavorite) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite",
                                tint = textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    if (contact.primaryNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = PhoneNumberHelper.formatForDisplay(contact.primaryNumber),
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted,
                            maxLines = 1
                        )
                    }
                }

                if (!isSelectionMode && contact.primaryNumber.isNotBlank()) {
                    FrostIconButton(
                        icon = Icons.Default.Call,
                        contentDescription = "Call ${contact.name}",
                        onClick = onCallClick,
                        size = 38.dp,
                        iconSize = 18.dp,
                        elevation = 1.dp
                    )
                }
            }
        }
    }
}
