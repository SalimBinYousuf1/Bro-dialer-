package com.example.ui.recents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.History
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.domain.usecase.PhoneNumberHelper
import com.example.ui.components.SalimAvatar
import com.example.ui.components.SalimConfirmationDialog
import com.example.ui.components.SalimEmptyState
import com.example.ui.theme.BrandColors
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostIconButton
import com.example.ui.theme.FrostSegmentedTabs
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.GlassBackgroundLight
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(
    viewModel: RecentsViewModel,
    onContactDetailClick: (String) -> Unit,
    onEditBeforeCall: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val calls by viewModel.filteredCalls.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var activeRecordForLongHoldSheet by remember { mutableStateOf<CallRecord?>(null) }

    // Group calls by Today, Yesterday, Older
    val groupedCalls = remember(calls) {
        val now = Calendar.getInstance()
        val todayYear = now.get(Calendar.YEAR)
        val todayDay = now.get(Calendar.DAY_OF_YEAR)

        val cal = Calendar.getInstance()
        calls.groupBy { record ->
            cal.timeInMillis = record.date
            val year = cal.get(Calendar.YEAR)
            val day = cal.get(Calendar.DAY_OF_YEAR)

            when {
                year == todayYear && day == todayDay -> "Today"
                year == todayYear && day == todayDay - 1 -> "Yesterday"
                else -> {
                    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    sdf.format(Date(record.date))
                }
            }
        }
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
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar (Title + Icon Actions ONLY, no text buttons, no crowding)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSelectionMode) "Selected (${selectedIds.size})" else "Recents",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        ),
                        color = textPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelectionMode) {
                            // Select All / Deselect All Icon Button
                            val allSelected = calls.isNotEmpty() && selectedIds.size == calls.size
                            FrostIconButton(
                                icon = if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                                contentDescription = if (allSelected) "Deselect All" else "Select All",
                                onClick = {
                                    if (allSelected) viewModel.deselectAll() else viewModel.selectAll()
                                },
                                size = 40.dp,
                                iconSize = 20.dp,
                                testTag = "recents_select_all_toggle"
                            )

                            // Delete Selected Icon Button (Highlighted when items selected)
                            FrostIconButton(
                                icon = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                enabled = selectedIds.isNotEmpty(),
                                tint = if (selectedIds.isNotEmpty()) BrandColors.AppleRed else textMuted.copy(alpha = 0.4f),
                                onClick = { showDeleteConfirmDialog = true },
                                size = 40.dp,
                                iconSize = 20.dp,
                                testTag = "recents_delete_selected"
                            )

                            // Cancel Selection Icon Button
                            FrostIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Cancel Selection",
                                onClick = { viewModel.setSelectionMode(false) },
                                size = 40.dp,
                                iconSize = 20.dp,
                                testTag = "recents_cancel_selection"
                            )
                        } else {
                            if (calls.isNotEmpty()) {
                                // Toggle Select Mode Icon Button
                                FrostIconButton(
                                    icon = Icons.Default.Checklist,
                                    contentDescription = "Select Calls",
                                    onClick = { viewModel.setSelectionMode(true) },
                                    size = 40.dp,
                                    iconSize = 20.dp,
                                    testTag = "recents_enter_selection"
                                )

                                // Clear All Icon Button
                                FrostIconButton(
                                    icon = Icons.Default.ClearAll,
                                    contentDescription = "Clear All Calls",
                                    onClick = { showClearAllConfirmDialog = true },
                                    size = 40.dp,
                                    iconSize = 20.dp,
                                    testTag = "recents_clear_all_button"
                                )
                            }
                        }
                    }
                }

                // Segmented Control Tabs (All Calls vs Missed)
                FrostSegmentedTabs(
                    tabs = listOf("All Calls", "Missed"),
                    selectedIndex = if (filter == RecentsFilter.ALL) 0 else 1,
                    onTabSelected = { index ->
                        viewModel.setFilter(if (index == 0) RecentsFilter.ALL else RecentsFilter.MISSED)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Content List or Empty State
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
                } else if (calls.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        SalimEmptyState(
                            icon = Icons.Outlined.History,
                            title = if (filter == RecentsFilter.ALL) "No Recent Calls" else "No Missed Calls",
                            description = if (filter == RecentsFilter.ALL) {
                                "Outgoing, incoming, and missed calls will be organized here."
                            } else {
                                "You have caught up with all your calls."
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupedCalls.forEach { (header, records) ->
                            item(key = "header_$header") {
                                Text(
                                    text = header.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 12.sp
                                    ),
                                    color = textMuted,
                                    modifier = Modifier.padding(start = 6.dp, top = 10.dp, bottom = 4.dp)
                                )
                            }

                            items(records, key = { it.id }) { record ->
                                val isSelected = selectedIds.contains(record.id)
                                RecentsGlassRowItem(
                                    record = record,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = isSelected,
                                    onPillClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(record.id)
                                        } else {
                                            viewModel.makeCall(record.number)
                                        }
                                    },
                                    onPillLongClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(record.id)
                                        } else {
                                            activeRecordForLongHoldSheet = record
                                        }
                                    },
                                    onInfoClick = {
                                        onContactDetailClick(record.number)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showDeleteConfirmDialog) {
        SalimConfirmationDialog(
            title = "Delete Selected Calls",
            message = "Are you sure you want to delete ${selectedIds.size} selected call log(s)?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteSelected()
                showDeleteConfirmDialog = false
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    if (showClearAllConfirmDialog) {
        SalimConfirmationDialog(
            title = "Clear Entire Call History",
            message = "This will permanently delete all call logs from your history.",
            confirmLabel = "Clear All",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllCallHistory()
                showClearAllConfirmDialog = false
            },
            onDismiss = { showClearAllConfirmDialog = false }
        )
    }

    // LONG-PRESS CONTEXT SHEET: Add to Blacklist, Copy Number, Edit Before Call, Send Message, Call
    activeRecordForLongHoldSheet?.let { record ->
        ModalBottomSheet(
            onDismissRequest = { activeRecordForLongHoldSheet = null },
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
                    name = record.callerName ?: record.number,
                    photoUri = record.photoUri,
                    size = 64.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                val isMissed = record.type == CallType.MISSED || record.type == CallType.REJECTED
                Text(
                    text = record.displayName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isMissed) BrandColors.AppleRed else textPrimary
                )
                if (record.callerName != null) {
                    Text(
                        text = PhoneNumberHelper.formatForDisplay(record.number),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                val timeStr = SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(record.date))
                val durStr = if (record.durationSeconds > 0) " (${formatDuration(record.durationSeconds)})" else ""
                Text(
                    text = "$timeStr$durStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action 1: Call
                FrostButton(
                    text = "Call ${PhoneNumberHelper.formatForDisplay(record.number)}",
                    icon = Icons.Default.Call,
                    onClick = {
                        val num = record.number
                        activeRecordForLongHoldSheet = null
                        viewModel.makeCall(num)
                    },
                    isProminent = true,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "recents_longhold_call"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action 2: Send Message
                FrostButton(
                    text = "Send Message",
                    icon = Icons.Default.Chat,
                    onClick = {
                        val num = record.number
                        activeRecordForLongHoldSheet = null
                        viewModel.sendSms(num)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "recents_longhold_message"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action 3: Edit Before Call
                FrostButton(
                    text = "Edit Number Before Call",
                    icon = Icons.Default.Edit,
                    onClick = {
                        val num = record.number
                        activeRecordForLongHoldSheet = null
                        onEditBeforeCall(num)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "recents_longhold_edit_before_call"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action 4: Copy Number
                FrostButton(
                    text = "Copy Number",
                    icon = Icons.Default.ContentCopy,
                    onClick = {
                        clipboardManager.setText(AnnotatedString(record.number))
                        activeRecordForLongHoldSheet = null
                        scope.launch { snackbarHostState.showSnackbar("Number copied to clipboard") }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "recents_longhold_copy"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action 5: Add to Blacklist / Block Number
                FrostButton(
                    text = "Add to Blacklist",
                    icon = Icons.Default.Block,
                    onClick = {
                        val num = record.number
                        val name = record.callerName
                        activeRecordForLongHoldSheet = null
                        viewModel.blockNumber(num, name) {
                            scope.launch { snackbarHostState.showSnackbar("Number added to Blacklist") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "recents_longhold_blacklist"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action 6: Remove from Recents
                FrostButton(
                    text = "Delete from Recents",
                    icon = Icons.Default.Delete,
                    onClick = {
                        viewModel.deleteCall(record.id)
                        activeRecordForLongHoldSheet = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "recents_longhold_delete"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentsGlassRowItem(
    record: CallRecord,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onPillClick: () -> Unit,
    onPillLongClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    val isMissed = record.type == CallType.MISSED || record.type == CallType.REJECTED
    // Apple standard: missed calls always display numbers/names in Apple iOS Red #FF3B30
    val callerColor = if (isMissed) BrandColors.AppleRed else textPrimary

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = textPrimary,
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 8.dp)
            )
        }

        // Clickable Numbers Pill (Separate from Info Button)
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .liquidGlass(
                    shape = RoundedCornerShape(16.dp),
                    elevation = 2.dp,
                    isElevated = isSelected
                )
                .combinedClickable(
                    onClick = onPillClick,
                    onLongClick = onPillLongClick
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                SalimAvatar(
                    name = record.callerName ?: record.number,
                    photoUri = record.photoUri,
                    size = 44.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Name/Number & Call details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isMissed) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = callerColor,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (typeIcon, typeLabel) = when (record.type) {
                            CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived to "Incoming"
                            CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade to "Outgoing"
                            CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed to "Missed"
                            CallType.REJECTED -> Icons.AutoMirrored.Filled.CallMissed to "Declined"
                            else -> Icons.Default.Call to "Call"
                        }

                        Icon(
                            imageVector = typeIcon,
                            contentDescription = typeLabel,
                            tint = if (isMissed) BrandColors.AppleRed else textMuted,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(record.date))
                        val durationStr = if (record.durationSeconds > 0) " • ${formatDuration(record.durationSeconds)}" else ""

                        Text(
                            text = "$typeLabel • $timeStr$durationStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // SEPARATE Info Button (Independent circular button, never triggers call, opens details)
        FrostIconButton(
            icon = Icons.Default.Info,
            contentDescription = "Details for ${record.displayName}",
            onClick = onInfoClick,
            size = 42.dp,
            iconSize = 20.dp,
            elevation = 1.dp,
            testTag = "recents_info_button_${record.id}"
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
