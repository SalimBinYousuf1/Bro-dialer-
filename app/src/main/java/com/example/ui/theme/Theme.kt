package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.model.ThemeMode

object NoRippleIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return object : Modifier.Node() {}
    }

    override fun hashCode(): Int = -1
    override fun equals(other: Any?): Boolean = other === this
}

private val DarkColorScheme = darkColorScheme(
    primary = GlassTextPrimaryDark,
    onPrimary = GlassBackgroundDark,
    primaryContainer = GlassSurfaceElevatedDark,
    onPrimaryContainer = GlassTextPrimaryDark,
    secondary = GlassTextSecondaryDark,
    onSecondary = GlassBackgroundDark,
    background = GlassBackgroundDark,
    onBackground = GlassTextPrimaryDark,
    surface = GlassSurfaceDark,
    onSurface = GlassTextPrimaryDark,
    surfaceVariant = GlassSecondarySurfaceDark,
    onSurfaceVariant = GlassTextSecondaryDark,
    outline = GlassBorderDark,
    error = GlassTextPrimaryDark,
    onError = GlassBackgroundDark
)

private val LightColorScheme = lightColorScheme(
    primary = GlassTextPrimaryLight,
    onPrimary = GlassPureWhite,
    primaryContainer = GlassSurfaceElevatedLight,
    onPrimaryContainer = GlassTextPrimaryLight,
    secondary = GlassTextSecondaryLight,
    onSecondary = GlassPureWhite,
    background = GlassBackgroundLight,
    onBackground = GlassTextPrimaryLight,
    surface = GlassSurfaceLight,
    onSurface = GlassTextPrimaryLight,
    surfaceVariant = GlassSecondarySurfaceLight,
    onSurfaceVariant = GlassTextSecondaryLight,
    outline = GlassBorderLight,
    error = GlassTextPrimaryLight,
    onError = GlassPureWhite
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalimTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val effectiveDark = when (themeMode) {
        ThemeMode.SYSTEM -> darkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SALIM -> false
    }

    val colorScheme = if (effectiveDark) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !effectiveDark
                    isAppearanceLightNavigationBars = !effectiveDark
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalIndication provides NoRippleIndication,
            LocalRippleConfiguration provides null
        ) {
            content()
        }
    }
}
