package com.example.ui.favorites

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.ui.components.SalimAvatar
import com.example.ui.components.SalimBackButton
import com.example.ui.components.SalimEmptyState
import com.example.ui.contacts.ContactsViewModel
import com.example.ui.theme.FrostIconButton
import com.example.ui.theme.FrostInteractiveCard
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight

@Composable
fun FavoritesScreen(
    viewModel: ContactsViewModel,
    onContactClick: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val contacts by viewModel.rawContacts.collectAsState()
    val favorites = contacts.filter { it.isFavorite }

    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                SalimBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = textPrimary
                )
            }

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    SalimEmptyState(
                        icon = Icons.Default.Star,
                        title = "No Favorites Yet",
                        description = "Star contacts to keep them here for quick one-tap calling.",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favorites, key = { it.id }) { contact ->
                        FavoriteGlassRow(
                            contact = contact,
                            onClick = { onContactClick(contact.id) },
                            onCall = { viewModel.makeCall(contact.primaryNumber) },
                            onMessage = { viewModel.sendSms(contact.primaryNumber) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteGlassRow(
    contact: ContactItem,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit
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
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = textPrimary,
                    maxLines = 1
                )
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

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (contact.primaryNumber.isNotBlank()) {
                    FrostIconButton(
                        icon = Icons.Default.Message,
                        contentDescription = "Message",
                        onClick = onMessage,
                        size = 38.dp,
                        iconSize = 18.dp,
                        elevation = 1.dp
                    )
                    FrostIconButton(
                        icon = Icons.Default.Call,
                        contentDescription = "Call",
                        onClick = onCall,
                        size = 38.dp,
                        iconSize = 18.dp,
                        elevation = 1.dp
                    )
                }
            }
        }
    }
}
