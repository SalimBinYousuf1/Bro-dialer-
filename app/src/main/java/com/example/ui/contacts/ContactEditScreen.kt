package com.example.ui.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SalimAvatar
import com.example.ui.components.SalimAvatarPickerSheet
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditScreen(
    onBack: () -> Unit,
    initialNumber: String = "",
    modifier: Modifier = Modifier,
    viewModel: ContactsViewModel = viewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(initialNumber) }
    var email by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var selectedAvatarUri by remember { mutableStateOf<String?>(null) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Contact",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    TextButton(
                        onClick = {
                            if (!isSaving) {
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
                                organization = organization,
                                avatarUri = selectedAvatarUri
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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Apple-style interactive Avatar Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .clickable { showAvatarPicker = true }
                        .testTag("edit_avatar_trigger")
                ) {
                    SalimAvatar(
                        name = "$firstName $lastName".trim().ifEmpty { "New" },
                        photoUri = selectedAvatarUri,
                        size = 96.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedAvatarUri != null) "Edit Photo" else "Add Photo",
                        color = SalimBlue,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

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
                            placeholder = "Company",
                            testTag = "contact_org_input"
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
                            keyboardType = KeyboardType.Phone,
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
                            keyboardType = KeyboardType.Email,
                            testTag = "contact_email_input"
                        )
                    }
                }
            }

            if (isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(0.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SalimBlue)
                }
            }

            // Avatar Selection Modal
            if (showAvatarPicker) {
                SalimAvatarPickerSheet(
                    currentAvatarUri = selectedAvatarUri,
                    contactName = "$firstName $lastName".trim().ifEmpty { "New Contact" },
                    onAvatarSelected = { uri ->
                        selectedAvatarUri = uri
                    },
                    onDismiss = { showAvatarPicker = false }
                )
            }
        }
    }
}

@Composable
private fun SalimInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = if (keyboardType == KeyboardType.Text) KeyboardCapitalization.Words else KeyboardCapitalization.None,
            keyboardType = keyboardType
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
