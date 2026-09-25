package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.FrostBackButton
import com.example.ui.theme.FrostButton
import com.example.ui.theme.FrostConfirmationDialog
import com.example.ui.theme.FrostSearchBar
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.GlassBackgroundLight
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass

data class AvatarPresetDef(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val bgColorDark: Color,
    val bgColorLight: Color
)

// Pure Neutral Physical Glass Presets (Charcoal, Smoke, Slate, Platinum, Pearl)
val AVATAR_PRESETS = listOf(
    AvatarPresetDef("preset:star", "VIP", Icons.Default.Star, Color(0x38FFFFFF), Color(0xEDEAE6)),
    AvatarPresetDef("preset:heart", "Favorite", Icons.Default.Favorite, Color(0x38FFFFFF), Color(0xEDEAE6)),
    AvatarPresetDef("preset:work", "Office", Icons.Default.Work, Color(0x38FFFFFF), Color(0xEDEAE6)),
    AvatarPresetDef("preset:home", "Home", Icons.Default.Home, Color(0x38FFFFFF), Color(0xEDEAE6)),
    AvatarPresetDef("preset:tech", "Tech", Icons.Default.Code, Color(0x38FFFFFF), Color(0xEDEAE6)),
    AvatarPresetDef("preset:art", "Creative", Icons.Default.Palette, Color(0x38FFFFFF), Color(0xEDEAE6)),
    AvatarPresetDef("preset:coffee", "Friend", Icons.Default.LocalCafe, Color(0x38FFFFFF), Color(0xEDEAE6)),
    AvatarPresetDef("preset:fitness", "Active", Icons.Default.FitnessCenter, Color(0x38FFFFFF), Color(0xEDEAE6))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalimTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp), elevation = 4.dp)
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    color = textPrimary
                )
            },
            navigationIcon = {
                navigationIcon?.invoke()
            },
            actions = {
                actions?.invoke()
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = textPrimary,
                actionIconContentColor = textPrimary,
                navigationIconContentColor = textPrimary
            )
        )
    }
}

@Composable
fun SalimBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FrostBackButton(onClick = onClick, modifier = modifier)
}

@Composable
fun SalimSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search contacts, numbers...",
    modifier: Modifier = Modifier
) {
    FrostSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = placeholder,
        modifier = modifier
    )
}

@Composable
fun SalimAvatar(
    name: String,
    photoUri: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val preset = if (photoUri?.startsWith("preset:") == true) {
        AVATAR_PRESETS.find { it.id == photoUri }
    } else null

    val bgGlass = if (preset != null) {
        if (dark) preset.bgColorDark else preset.bgColorLight
    } else {
        if (dark) Color(0x38FFFFFF) else Color(0xEDEAE6)
    }

    val iconOrTextColor = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight

    Box(
        modifier = modifier
            .size(size)
            .liquidGlass(shape = CircleShape, elevation = 2.dp)
            .clip(CircleShape)
            .background(bgGlass),
        contentAlignment = Alignment.Center
    ) {
        if (preset != null) {
            Icon(
                imageVector = preset.icon,
                contentDescription = preset.label,
                tint = iconOrTextColor,
                modifier = Modifier.size((size.value * 0.5f).dp)
            )
        } else if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Photo for $name",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = if (name.isNotBlank()) {
                val parts = name.trim().split("\\s+".toRegex())
                if (parts.size >= 2) {
                    "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                } else {
                    parts[0].take(1).uppercase()
                }
            } else "?"

            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (size.value * 0.38f).sp
                ),
                color = iconOrTextColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalimAvatarPickerSheet(
    currentAvatarUri: String?,
    contactName: String,
    onAvatarSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onAvatarSelected(uri.toString())
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Contact Avatar",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Current Preview
            SalimAvatar(
                name = contactName,
                photoUri = currentAvatarUri,
                size = 88.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Photo picker button
            FrostButton(
                text = "Choose from Photos",
                icon = Icons.Default.PhotoLibrary,
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                isProminent = true,
                modifier = Modifier.fillMaxWidth(),
                testTag = "pick_photo_from_gallery"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "OR CHOOSE NEUTRAL PRESET",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4x2 Grid of Presets
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(AVATAR_PRESETS, key = { it.id }) { preset ->
                    val isSelected = currentAvatarUri == preset.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAvatarSelected(preset.id)
                                onDismiss()
                            }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .liquidGlass(shape = CircleShape, elevation = 2.dp, isElevated = isSelected)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            2.dp,
                                            if (dark) GlassBorderDark else GlassBorderLight,
                                            CircleShape
                                        )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = preset.icon,
                                contentDescription = preset.label,
                                tint = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!currentAvatarUri.isNullOrBlank()) {
                FrostButton(
                    text = "Reset to Monogram Initials",
                    onClick = {
                        onAvatarSelected(null)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "reset_avatar_button"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SalimEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .liquidGlass(shape = CircleShape, elevation = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            FrostButton(
                text = actionLabel,
                onClick = onActionClick,
                isProminent = true
            )
        }
    }
}

@Composable
fun SalimConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    cancelLabel: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    FrostConfirmationDialog(
        title = title,
        message = message,
        confirmLabel = confirmLabel,
        cancelLabel = cancelLabel,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
