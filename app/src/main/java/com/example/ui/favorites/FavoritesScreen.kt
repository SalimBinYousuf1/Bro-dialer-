package com.example.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.ui.components.SalimAvatar
import com.example.ui.components.SalimBackButton
import com.example.ui.components.SalimEmptyState
import com.example.ui.components.SalimTopAppBar
import com.example.ui.contacts.ContactsViewModel
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimYellow

@Composable
fun FavoritesScreen(
    viewModel: ContactsViewModel,
    onContactClick: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.rawContacts.collectAsState()
    val favorites = contacts.filter { it.isFavorite }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SalimTopAppBar(
                title = "Favorites",
                navigationIcon = {
                    SalimBackButton(onClick = onBack)
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
            if (favorites.isEmpty()) {
                SalimEmptyState(
                    icon = Icons.Default.Star,
                    title = "No Favorites Yet",
                    description = "Star contacts in your address book to add them to your favorites for instant access.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("favorites_list"),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(favorites, key = { it.id }) { contact ->
                        FavoriteContactRow(
                            contact = contact,
                            onClick = { onContactClick(contact.id) },
                            onCall = { viewModel.makeCall(contact.primaryNumber) },
                            onMessage = { viewModel.sendSms(contact.primaryNumber) },
                            onToggleFavorite = { viewModel.toggleFavorite(contact) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteContactRow(
    contact: ContactItem,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SalimAvatar(
                name = contact.name,
                photoUri = contact.photoUri,
                size = 48.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = contact.primaryNumber.ifBlank { "No phone number" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (contact.primaryNumber.isNotBlank()) {
                IconButton(
                    onClick = onCall,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = SalimGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onMessage,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Message",
                        tint = SalimBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Unfavorite",
                    tint = SalimYellow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 82.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    }
}
