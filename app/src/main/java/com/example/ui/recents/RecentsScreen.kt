package com.example.ui.recents

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.ui.components.SalimAvatar
import com.example.ui.components.SalimConfirmationDialog
import com.example.ui.components.SalimEmptyState
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostCard
import com.example.ui.theme.FrostIconButton
import com.example.ui.theme.FrostSegmentedTabs
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.GlassBackgroundLight
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.liquidGlassInteractive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RecentsScreen(
    viewModel: RecentsViewModel,
    onContactDetailClick: (String) -> Unit,
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

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recents",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = textPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isSelectionMode) {
                        FrostButton(
                            text = "Cancel",
                            onClick = { viewModel.setSelectionMode(false) },
                            testTag = "recents_cancel_selection"
                        )
                        if (selectedIds.isNotEmpty()) {
                            FrostButton(
                                text = "Delete (${selectedIds.size})",
                                onClick = { showDeleteConfirmDialog = true },
                                isProminent = true,
                                testTag = "recents_delete_selected"
                            )
                        }
                    } else if (calls.isNotEmpty()) {
                        FrostIconButton(
                            icon = Icons.Default.ClearAll,
                            contentDescription = "Clear All",
                            onClick = { showClearAllConfirmDialog = true },
                            testTag = "recents_clear_all_button"
                        )
                    }
                }
            }

            // Segmented Control Tabs (All vs Missed)
            FrostSegmentedTabs(
                tabs = listOf("All Calls", "Missed"),
                selectedIndex = if (filter == RecentsFilter.ALL) 0 else 1,
                onTabSelected = { index ->
                    viewModel.setFilter(if (index == 0) RecentsFilter.ALL else RecentsFilter.MISSED)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
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
                                modifier = Modifier.padding(start = 6.dp, top = 12.dp, bottom = 4.dp)
                            )
                        }

                        items(records, key = { it.id }) { record ->
                            val isSelected = selectedIds.contains(record.id)
                            RecentsGlassItem(
                                record = record,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelectionMode) {
                                        viewModel.toggleSelection(record.id)
                                    } else {
                                        viewModel.makeCall(record.number)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        viewModel.setSelectionMode(true)
                                        viewModel.toggleSelection(record.id)
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

    // Confirmation Dialogs
    if (showDeleteConfirmDialog) {
        SalimConfirmationDialog(
            title = "Delete Call Records",
            message = "Are you sure you want to delete ${selectedIds.size} selected call log(s)?",
            confirmLabel = "Delete",
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
            onConfirm = {
                viewModel.clearAllCallHistory()
                showClearAllConfirmDialog = false
            },
            onDismiss = { showClearAllConfirmDialog = false }
        )
    }
}

@Composable
fun RecentsGlassItem(
    record: CallRecord,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassInteractive(
                shape = shape,
                elevation = 2.dp,
                isElevated = isSelected,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
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
                        .size(24.dp)
                        .padding(end = 6.dp)
                )
            }

            // Contact Avatar or Initials
            SalimAvatar(
                name = record.callerName ?: record.number,
                photoUri = record.photoUri,
                size = 44.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Caller name, call type icon, formatted time
            Column(modifier = Modifier.weight(1f)) {
                val displayName = record.displayName
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (record.type == CallType.MISSED) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = textPrimary,
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
                        tint = textMuted,
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

            // Info action button
            FrostIconButton(
                icon = Icons.Default.Info,
                contentDescription = "Details",
                onClick = onInfoClick,
                size = 36.dp,
                iconSize = 18.dp,
                elevation = 1.dp
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
