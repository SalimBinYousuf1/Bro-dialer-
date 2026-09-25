package com.example.ui.blocked

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockedNumber
import com.example.ui.components.SalimBackButton
import com.example.ui.components.SalimConfirmationDialog
import com.example.ui.components.SalimEmptyState
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostCard
import com.example.ui.theme.FrostIconButton
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.GlassBackgroundLight
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight

@Composable
fun BlockedNumbersScreen(
    viewModel: BlockedNumbersViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val blockedList by viewModel.blockedNumbers.collectAsState()

    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    var showAddDialog by remember { mutableStateOf(false) }
    var numberToUnblock by remember { mutableStateOf<BlockedNumber?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SalimBackButton(onClick = onBack)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Blocked Numbers",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = textPrimary
                    )
                }

                FrostIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Block Number",
                    onClick = { showAddDialog = true },
                    size = 42.dp,
                    iconSize = 20.dp,
                    testTag = "add_blocked_number_button"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Calls from blocked numbers will be declined immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = textMuted,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            if (blockedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    SalimEmptyState(
                        icon = Icons.Default.Block,
                        title = "No Blocked Numbers",
                        description = "Numbers you block will appear here. You won't receive calls or notifications from them.",
                        actionLabel = "Block a Number",
                        onActionClick = { showAddDialog = true }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(blockedList, key = { it.id }) { item ->
                        FrostCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.contactName ?: item.number,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = textPrimary
                                    )
                                    if (item.contactName != null) {
                                        Text(
                                            text = item.number,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textMuted
                                        )
                                    }
                                }

                                FrostButton(
                                    text = "Unblock",
                                    onClick = { numberToUnblock = item }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Blocked Number Dialog
    if (showAddDialog) {
        var inputNumber by remember { mutableStateOf("") }
        var inputName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Block a Number",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputNumber,
                        onValueChange = { inputNumber = it },
                        label = { Text("Phone number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            cursorColor = textPrimary,
                            focusedBorderColor = textPrimary,
                            unfocusedBorderColor = textMuted.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Name (optional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            cursorColor = textPrimary,
                            focusedBorderColor = textPrimary,
                            unfocusedBorderColor = textMuted.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                FrostButton(
                    text = "Block",
                    isProminent = true,
                    onClick = {
                        if (inputNumber.isNotBlank()) {
                            viewModel.blockNumber(
                                number = inputNumber.trim(),
                                contactName = inputName.trim().ifBlank { null }
                            )
                            showAddDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                FrostButton(
                    text = "Cancel",
                    onClick = { showAddDialog = false }
                )
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
        )
    }

    // Unblock confirmation
    if (numberToUnblock != null) {
        val item = numberToUnblock!!
        SalimConfirmationDialog(
            title = "Unblock Number",
            message = "Are you sure you want to unblock ${item.contactName ?: item.number}?",
            confirmLabel = "Unblock",
            onConfirm = {
                viewModel.unblock(item)
                numberToUnblock = null
            },
            onDismiss = { numberToUnblock = null }
        )
    }
}
