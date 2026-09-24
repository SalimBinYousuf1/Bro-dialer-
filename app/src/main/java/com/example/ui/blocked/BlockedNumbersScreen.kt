package com.example.ui.blocked

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockedNumber
import com.example.ui.components.SalimBackButton
import com.example.ui.components.SalimConfirmationDialog
import com.example.ui.components.SalimEmptyState
import com.example.ui.components.SalimTopAppBar
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimRed
import com.example.ui.theme.SalimWhite

@Composable
fun BlockedNumbersScreen(
    viewModel: BlockedNumbersViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val blockedList by viewModel.blockedNumbers.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var numberToUnblock by remember { mutableStateOf<BlockedNumber?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SalimTopAppBar(
                title = "Blocked Numbers",
                navigationIcon = {
                    SalimBackButton(onClick = onBack)
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_blocked_number_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Blocked Number",
                            tint = SalimBlue
                        )
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (blockedList.isEmpty()) {
                SalimEmptyState(
                    icon = Icons.Default.Block,
                    title = "No Blocked Numbers",
                    description = "When you block numbers, calls from them will be automatically screened or rejected.",
                    actionLabel = "Block a Number",
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("blocked_numbers_list"),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(blockedList, key = { it.id }) { item ->
                        BlockedNumberRow(
                            item = item,
                            onUnblock = { numberToUnblock = item }
                        )
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
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the phone number you wish to block from calling.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = inputNumber,
                        onValueChange = { inputNumber = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("block_input_number")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("block_input_name")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputNumber.isNotBlank()) {
                            viewModel.blockNumber(inputNumber, inputName.ifBlank { null })
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SalimRed,
                        contentColor = SalimWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Block")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Unblock Confirmation
    numberToUnblock?.let { item ->
        SalimConfirmationDialog(
            title = "Unblock Number",
            message = "Calls and messages from ${item.contactName ?: item.number} will no longer be blocked. Continue?",
            confirmLabel = "Unblock",
            isDestructive = false,
            onConfirm = {
                viewModel.unblock(item)
                numberToUnblock = null
            },
            onDismiss = { numberToUnblock = null }
        )
    }
}

@Composable
private fun BlockedNumberRow(
    item: BlockedNumber,
    onUnblock: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.contactName ?: item.number,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (item.contactName != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.number,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TextButton(
                onClick = onUnblock,
                modifier = Modifier.testTag("unblock_button_${item.id}")
            ) {
                Text(
                    text = "Unblock",
                    color = SalimBlue,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    }
}
