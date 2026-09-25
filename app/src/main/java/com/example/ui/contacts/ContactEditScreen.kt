package com.example.ui.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostCard
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.GlassBackgroundLight
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditScreen(
    onBack: () -> Unit,
    initialNumber: String = "",
    modifier: Modifier = Modifier,
    viewModel: ContactsViewModel = viewModel()
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

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
        containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Contact",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = textPrimary
                ),
                navigationIcon = {
                    FrostButton(
                        text = "Cancel",
                        onClick = { if (!isSaving) onBack() },
                        testTag = "contact_cancel_button"
                    )
                },
                actions = {
                    FrostButton(
                        text = "Done",
                        onClick = {
                            if (firstName.isBlank() && lastName.isBlank() && phoneNumber.isBlank()) {
                                errorMessage = "Please enter a name or phone number"
                                return@FrostButton
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
                        isProminent = true,
                        testTag = "contact_save_button"
                    )
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
                // Interactive Avatar Header
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
                        color = textPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = textPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Name Details Card
                FrostCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        GlassContactTextField(
                            value = firstName,
                            onValueChange = { firstName = it; errorMessage = null },
                            placeholder = "First name",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            testTag = "contact_first_name_input"
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = textMuted.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        GlassContactTextField(
                            value = lastName,
                            onValueChange = { lastName = it; errorMessage = null },
                            placeholder = "Last name",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            testTag = "contact_last_name_input"
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = textMuted.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        GlassContactTextField(
                            value = organization,
                            onValueChange = { organization = it },
                            placeholder = "Company / Organization",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            testTag = "contact_organization_input"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Phone Details Card
                FrostCard(modifier = Modifier.fillMaxWidth()) {
                    GlassContactTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it; errorMessage = null },
                        placeholder = "Phone number",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        testTag = "contact_phone_input"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Email Details Card
                FrostCard(modifier = Modifier.fillMaxWidth()) {
                    GlassContactTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        testTag = "contact_email_input"
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            if (isSaving) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = textPrimary)
                }
            }
        }
    }

    if (showAvatarPicker) {
        SalimAvatarPickerSheet(
            currentAvatarUri = selectedAvatarUri,
            contactName = "$firstName $lastName".trim().ifEmpty { "New Contact" },
            onAvatarSelected = { uri ->
                selectedAvatarUri = uri
                showAvatarPicker = false
            },
            onDismiss = { showAvatarPicker = false }
        )
    }
}

@Composable
private fun GlassContactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    testTag: String
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = placeholder, color = textMuted.copy(alpha = 0.7f))
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = textPrimary,
            focusedTextColor = textPrimary,
            unfocusedTextColor = textPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
