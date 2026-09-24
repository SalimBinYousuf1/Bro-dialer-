package com.example.ui.recents

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.ui.components.SalimConfirmationDialog
import com.example.ui.components.SalimEmptyState
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimRed
import com.example.ui.theme.SalimWhite
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
    val calls by viewModel.filteredCalls.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

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
                    val sdf = SimpleDateFormat("MMMM d", Locale.getDefault())
                    sdf.format(Date(record.date))
                }
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Row with Title, Segmented filter, and Edit/Done
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                if (selectedIds.isNotEmpty()) {
                                    viewModel.deselectAll()
                                } else {
                                    viewModel.selectAll()
                                }
                            },
                            modifier = Modifier.testTag("recents_select_all_button")
                        ) {
                            Text(
                                text = if (selectedIds.isNotEmpty()) "Deselect All" else "Select All",
                                color = SalimBlue,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Recents",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Segmented control (All / Missed)
                if (!isSelectionMode) {
                    RecentsSegmentedControl(
                        selectedFilter = filter,
                        onFilterSelected = viewModel::setFilter
                    )
                }

                if (isSelectionMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                viewModel.deselectAll()
                                viewModel.setSelectionMode(false)
                            },
                            modifier = Modifier.testTag("recents_cancel_button")
                        ) {
                            Text(
                                text = "Cancel",
                                color = SalimRed,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        TextButton(
                            onClick = {
                                viewModel.setSelectionMode(false)
                            },
                            modifier = Modifier.testTag("recents_done_button")
                        ) {
                            Text(
                                text = "Done",
                                color = SalimBlue,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                } else {
                    TextButton(
                        onClick = {
                            viewModel.setSelectionMode(true)
                        },
                        modifier = Modifier.testTag("recents_edit_button")
                    ) {
                        Text(
                            text = "Edit",
                            color = SalimBlue,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Main List or Empty State
            if (isLoading && calls.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SalimBlue)
                }
            } else if (calls.isEmpty()) {
                SalimEmptyState(
                    icon = Icons.Outlined.History,
                    title = if (filter == RecentsFilter.MISSED) "No Missed Calls" else "No Recent Calls",
                    description = if (filter == RecentsFilter.MISSED) {
                        "You don't have any missed calls in your call history."
                    } else {
                        "When you make or receive calls, they will appear here."
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("recents_list"),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    groupedCalls.forEach { (dateGroup, itemsInGroup) ->
                        item(key = "header_$dateGroup") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 20.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = dateGroup,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(
                            items = itemsInGroup,
                            key = { it.id }
                        ) { call ->
                            val isSelected = selectedIds.contains(call.id)
                            RecentCallRow(
                                record = call,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelectionMode) {
                                        viewModel.toggleSelection(call.id)
                                    } else {
                                        viewModel.makeCall(call.number)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        viewModel.setSelectionMode(true)
                                        viewModel.toggleSelection(call.id)
                                    }
                                },
                                onInfoClick = {
                                    onContactDetailClick(call.number)
                                }
                            )
                        }
                    }
                }
            }

            // Bottom action bar in Selection Mode
            if (isSelectionMode) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showClearAllConfirmDialog = true },
                            modifier = Modifier.testTag("recents_clear_all_button")
                        ) {
                            Text("Clear All", color = SalimRed, style = MaterialTheme.typography.bodyLarge)
                        }

                        Button(
                            onClick = {
                                if (selectedIds.isNotEmpty()) {
                                    showDeleteConfirmDialog = true
                                }
                            },
                            enabled = selectedIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SalimRed,
                                contentColor = SalimWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("recents_delete_selected_button")
                        ) {
                            Text("Delete (${selectedIds.size})")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        SalimConfirmationDialog(
            title = "Delete Call Records",
            message = "Are you sure you want to remove ${selectedIds.size} call records from your call log?",
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
            title = "Clear All Call History",
            message = "This will permanently remove all call records from your device's call log. Continue?",
            confirmLabel = "Clear All",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllCallHistory()
                showClearAllConfirmDialog = false
            },
            onDismiss = { showClearAllConfirmDialog = false }
        )
    }
}

@Composable
fun RecentsSegmentedControl(
    selectedFilter: RecentsFilter,
    onFilterSelected: (RecentsFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val allSelected = selectedFilter == RecentsFilter.ALL
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (allSelected) MaterialTheme.colorScheme.background else Color.Transparent)
                .clickable { onFilterSelected(RecentsFilter.ALL) }
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "All",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (allSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val missedSelected = selectedFilter == RecentsFilter.MISSED
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (missedSelected) MaterialTheme.colorScheme.background else Color.Transparent)
                .clickable { onFilterSelected(RecentsFilter.MISSED) }
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Missed",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (missedSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (missedSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentCallRow(
    record: CallRecord,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMissed = record.type == CallType.MISSED || record.type == CallType.REJECTED
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(record.date) { timeFormat.format(Date(record.date)) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("recent_row_${record.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isSelected) "Selected" else "Not selected",
                    tint = if (isSelected) SalimBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Direction icon
            val (dirIcon, dirTint) = when (record.type) {
                CallType.MISSED, CallType.REJECTED -> Icons.AutoMirrored.Filled.CallMissed to SalimRed
                CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade to SalimBlue
                else -> Icons.AutoMirrored.Filled.CallReceived to SalimGreen
            }

            Icon(
                imageVector = dirIcon,
                contentDescription = record.type.name,
                tint = dirTint,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    ),
                    color = if (isMissed) SalimRed else MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.number,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (record.durationSeconds > 0) {
                        Text(
                            text = " • ${record.formattedDuration}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = SalimBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 52.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}
