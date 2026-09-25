package com.example.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Core Liquid Glass Frost Material Engine.
 * Implements a pure neutral white/light physical-glass material system:
 * - Multi-layer transparency
 * - Specular upper-edge highlight
 * - Fine translucent rim
 * - Soft ambient shadow
 * - Restrained physical spring compression (0.97 scale on press)
 * - Gentle haptic feedback
 * - Strictly neutral (NO blue, green, red, or arbitrary colors)
 */

@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 3.dp,
    isElevated: Boolean = false,
    alphaMultiplier: Float = 1f
): Modifier {
    val dark = isSystemInDarkTheme()

    // Multi-layer frosted glass luminance gradients
    val baseTop = if (dark) {
        if (isElevated) Color(0x52FFFFFF) else Color(0x38FFFFFF)
    } else {
        if (isElevated) Color(0xF7FFFFFF) else Color(0xEDFFFFFF)
    }
    val baseBottom = if (dark) {
        if (isElevated) Color(0x33FFFFFF) else Color(0x21FFFFFF)
    } else {
        if (isElevated) Color(0xEBFFFFFF) else Color(0xDCFFFFFF)
    }

    // Specular upper-edge highlight rim (light refraction)
    val specularRimTop = if (dark) Color(0x66FFFFFF) else Color(0x99FFFFFF)
    val specularRimBottom = if (dark) Color(0x24FFFFFF) else Color(0x33000000)

    val shadowColor = if (dark) Color(0x80000000) else Color(0x1F000000)

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    baseTop.copy(alpha = (baseTop.alpha * alphaMultiplier).coerceIn(0f, 1f)),
                    baseBottom.copy(alpha = (baseBottom.alpha * alphaMultiplier).coerceIn(0f, 1f))
                )
            )
        )
        .border(
            BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(specularRimTop, specularRimBottom)
                )
            ),
            shape = shape
        )
}

/**
 * Interactive physical glass surface modifier with controlled spring compression and haptics.
 */
@Composable
fun Modifier.liquidGlassInteractive(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 3.dp,
    isElevated: Boolean = false,
    enabled: Boolean = true,
    testTag: String? = null,
    onClick: () -> Unit
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
        label = "glass_compression"
    )

    var mod = this
    if (testTag != null) {
        mod = mod.testTag(testTag)
    }

    return mod
        .scale(scale)
        .liquidGlass(
            shape = shape,
            elevation = if (isPressed) 1.dp else elevation,
            isElevated = isPressed || isElevated
        )
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                awaitFirstDown()
                isPressed = true
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                val up = waitForUpOrCancellation()
                isPressed = false
                if (up != null) {
                    onClick()
                }
            }
        }
}

/**
 * Universal Frost Button (Primary, Secondary, Wide).
 * Follows the neutral Liquid Glass Frost material with dark neutral text/icon and spring motion.
 */
@Composable
fun FrostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isProminent: Boolean = false,
    testTag: String? = null
) {
    val dark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(14.dp)
    val contentColor = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight

    Box(
        modifier = modifier
            .liquidGlassInteractive(
                shape = shape,
                elevation = if (isProminent) 4.dp else 2.5.dp,
                enabled = enabled,
                testTag = testTag,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isProminent) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 15.sp,
                    letterSpacing = 0.2.sp
                ),
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Universal Frost Icon Button (Circular or Rounded).
 */
@Composable
fun FrostIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp,
    shape: Shape = CircleShape,
    elevation: Dp = 2.dp,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val dark = isSystemInDarkTheme()
    val tint = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight

    Box(
        modifier = modifier
            .size(size)
            .liquidGlassInteractive(
                shape = shape,
                elevation = elevation,
                enabled = enabled,
                testTag = testTag,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Standard Neutral Glass Back Button.
 */
@Composable
fun FrostBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FrostIconButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        onClick = onClick,
        modifier = modifier,
        testTag = "back_button"
    )
}

/**
 * Standard Neutral Glass Close / Exit Button.
 */
@Composable
fun FrostCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FrostIconButton(
        icon = Icons.Default.Close,
        contentDescription = "Close",
        onClick = onClick,
        modifier = modifier,
        testTag = "close_button"
    )
}

/**
 * Neutral Physical-Glass Switch (Toggle).
 * NO BLUE, NO GREEN.
 * Distinguishable purely by physical knob position, material luminance, and internal specular highlight.
 */
@Composable
fun FrostSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val dark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current

    val trackWidth = 52.dp
    val trackHeight = 30.dp
    val thumbSize = 24.dp
    val thumbPadding = 3.dp

    val targetOffset = if (checked) 22.dp else 0.dp
    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset.value,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
        label = "switch_knob_offset"
    )

    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .size(trackWidth, trackHeight)
            .liquidGlass(
                shape = shape,
                elevation = if (checked) 3.dp else 1.5.dp,
                isElevated = checked
            )
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown()
                    val up = waitForUpOrCancellation()
                    if (up != null) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCheckedChange?.invoke(!checked)
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Sliding physical frosted thumb
        Box(
            modifier = Modifier
                .padding(start = thumbPadding)
                .offset { IntOffset(x = animatedOffset.dp.roundToPx(), y = 0) }
                .size(thumbSize)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    if (dark) {
                        if (checked) Color(0xFFFFFFFF) else Color(0xCCFFFFFF)
                    } else {
                        if (checked) Color(0xFF141416) else Color(0xCCFFFFFF)
                    }
                )
                .border(
                    BorderStroke(
                        1.dp,
                        if (dark) Color(0x4DFFFFFF) else Color(0x33000000)
                    ),
                    shape = CircleShape
                )
        )
    }
}

/**
 * Universal Frost Card Container.
 */
@Composable
fun FrostCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 2.5.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .liquidGlass(shape = shape, elevation = elevation)
            .padding(16.dp),
        content = content
    )
}

/**
 * Interactive Frost Card with Spring Compression.
 */
@Composable
fun FrostInteractiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 2.5.dp,
    testTag: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .liquidGlassInteractive(
                shape = shape,
                elevation = elevation,
                testTag = testTag,
                onClick = onClick
            )
            .padding(16.dp),
        content = content
    )
}

/**
 * Pure Neutral Glass Segmented Control / Tab Switcher.
 */
@Composable
fun FrostSegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val trackShape = RoundedCornerShape(16.dp)
    val pillShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = trackShape, elevation = 2.dp)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
                val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .then(
                            if (selected) {
                                Modifier.liquidGlass(
                                    shape = pillShape,
                                    elevation = 2.5.dp,
                                    isElevated = true
                                )
                            } else {
                                Modifier.clip(pillShape)
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp
                        ),
                        color = if (selected) textPrimary else textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Universal Frost Search Bar.
 */
@Composable
fun FrostSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search",
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val textPrimary = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
    val textMuted = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .liquidGlass(shape = shape, elevation = 2.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = textMuted,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textMuted.copy(alpha = 0.8f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = textPrimary,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(textPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input")
                )
            }
            if (query.isNotEmpty()) {
                FrostIconButton(
                    icon = Icons.Default.Clear,
                    contentDescription = "Clear search",
                    onClick = { onQueryChange("") },
                    size = 28.dp,
                    iconSize = 16.dp,
                    elevation = 1.dp,
                    testTag = "search_clear_button"
                )
            }
        }
    }
}

/**
 * Universal Neutral Liquid Glass Confirmation Dialog.
 */
@Composable
fun FrostConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    cancelLabel: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val dialogShape = RoundedCornerShape(22.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = if (dark) GlassTextPrimaryDark else GlassTextPrimaryLight
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (dark) GlassTextSecondaryDark else GlassTextSecondaryLight
            )
        },
        confirmButton = {
            FrostButton(
                text = confirmLabel,
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                isProminent = true,
                testTag = "dialog_confirm_button"
            )
        },
        dismissButton = {
            FrostButton(
                text = cancelLabel,
                onClick = onDismiss,
                testTag = "dialog_cancel_button"
            )
        },
        shape = dialogShape,
        containerColor = if (dark) GlassBackgroundDark else GlassBackgroundLight
    )
}
