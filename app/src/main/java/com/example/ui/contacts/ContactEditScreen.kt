package com.example.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SalimConfirmationDialog
import com.example.ui.components.SalimTopAppBar
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimRed
import kotlinx.coroutines.launch

@Composable
fun ContactEditScreen(
    initialNumber: String = "",
    viewModel: ContactsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(initialNumber) }
    var email by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val hasChanges = firstName.isNotBlank() || lastName.isNotBlank() || phoneNumber.isNotBlank() || email.isNotBlank() || organization.isNotBlank()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SalimTopAppBar(
                title = "New Contact",
                navigationIcon = {
                    TextButton(
                        onClick = {
                            if (hasChanges) {
                                showDiscardDialog = true
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("contact_cancel_button")
                    ) {
                        Text("Cancel", color = SalimBlue, style = MaterialTheme.typography.bodyLarge)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (firstName.isBlank() && lastName.isBlank() && phoneNumber.isBlank()) {
                                errorMessage = "Please enter a name or phone number"
                                return@TextButton
                            }
                            isSaving = true
                            viewModel.createContact(
                                firstName = firstName,
                                lastName = lastName,
                                phone = phoneNumber,
                                email = email,
                                organization = organization
                            ) { success ->
                                isSaving = false
                                if (success) {
                                    onBack()
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Failed to create contact")
                                    }
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.testTag("contact_save_button")
                    ) {
                        Text(
                            text = "Done",
                            color = SalimBlue,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = SalimRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SalimInputField(
                            value = firstName,
                            onValueChange = {
                                firstName = it
                                errorMessage = null
                            },
                            placeholder = "First name",
                            testTag = "contact_first_name_input"
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        SalimInputField(
                            value = lastName,
                            onValueChange = {
                                lastName = it
                                errorMessage = null
                            },
                            placeholder = "Last name",
                            testTag = "contact_last_name_input"
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        SalimInputField(
                            value = organization,
                            onValueChange = { organization = it },
                            placeholder = "Company / Organization",
                            testTag = "contact_company_input"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SalimInputField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                errorMessage = null
                            },
                            placeholder = "Phone number",
                            testTag = "contact_phone_input"
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        SalimInputField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Email address",
                            testTag = "contact_email_input"
                        )
                    }
                }
            }

            if (isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SalimBlue)
                }
            }
        }
    }

    if (showDiscardDialog) {
        SalimConfirmationDialog(
            title = "Discard Changes?",
            message = "You have unsaved changes. Are you sure you want to discard them?",
            confirmLabel = "Discard",
            isDestructive = true,
            onConfirm = onBack,
            onDismiss = { showDiscardDialog = false }
        )
    }
}

@Composable
private fun SalimInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
