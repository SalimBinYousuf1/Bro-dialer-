package com.example.ui.contacts

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.ui.components.SalimAvatar
import com.example.ui.components.SalimEmptyState
import com.example.ui.components.SalimSearchBar
import com.example.ui.theme.FrostIconButton
import com.example.ui.theme.FrostInteractiveCard
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    onContactClick: (Long) -> Unit,
    onAddContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val contacts by viewModel.filteredContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

    // Group contacts by first character of name
    val groupedContacts = remember(contacts) {
        contacts.groupBy {
            val first = it.name.trim().firstOrNull()?.uppercaseChar() ?: '#'
            if (first in 'A'..'Z') first else '#'
        }.toSortedMap()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp)
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
                    text = "Contacts",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = textPrimary
                )
                FrostIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add Contact",
                    onClick = onAddContactClick,
                    size = 44.dp,
                    iconSize = 22.dp,
                    elevation = 2.dp,
                    testTag = "add_contact_button"
                )
            }

            // Search Bar
            SalimSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = "Search contacts, phone numbers...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
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
                                    fontSize = 12.sp
                                ),
                                color = textMuted,
                                modifier = Modifier.padding(start = 6.dp, top = 10.dp, bottom = 4.dp)
                            )
                        }

                        items(contactList, key = { it.id }) { contact ->
                            ContactGlassRow(
                                contact = contact,
                                onClick = { onContactClick(contact.id) },
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

@Composable
fun ContactGlassRow(
    contact: ContactItem,
    onClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight
    val shape = RoundedCornerShape(16.dp)

    FrostInteractiveCard(
        onClick = onClick,
        shape = shape,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
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
                        text = contact.primaryNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = textMuted,
                        maxLines = 1
                    )
                }
            }

            if (contact.primaryNumber.isNotBlank()) {
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
