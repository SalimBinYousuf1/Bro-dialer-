package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassTextPrimaryDark
import com.example.ui.theme.GlassTextPrimaryLight
import com.example.ui.theme.GlassTextSecondaryDark
import com.example.ui.theme.GlassTextSecondaryLight
import com.example.ui.theme.liquidGlass

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home")
    data object Recents : BottomNavItem(Screen.Recents.route, "Recents", Icons.Filled.History, Icons.Outlined.History, "nav_recents")
    data object Contacts : BottomNavItem(Screen.Contacts.route, "Contacts", Icons.Filled.Person, Icons.Outlined.Person, "nav_contacts")
    data object Dialpad : BottomNavItem(Screen.Dialpad.route, "Dialpad", Icons.Filled.Dialpad, Icons.Outlined.Dialpad, "nav_dialpad")
    data object More : BottomNavItem(Screen.More.route, "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz, "nav_more")
}

@Composable
fun SalimBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Recents,
        BottomNavItem.Contacts,
        BottomNavItem.Dialpad,
        BottomNavItem.More
    )

    val barShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    val pillShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = barShape, elevation = 6.dp)
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
                val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.0f else 0.96f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
                    label = "nav_item_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .scale(scale)
                        .then(
                            if (selected) {
                                Modifier.liquidGlass(
                                    shape = pillShape,
                                    elevation = 2.dp,
                                    isElevated = true
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (!selected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onNavigate(item.route)
                                }
                            }
                        )
                        .testTag(item.testTag)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = if (selected) textPrimary else textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) textPrimary else textMuted
                        )
                    }
                }
            }
        }
    }
}
