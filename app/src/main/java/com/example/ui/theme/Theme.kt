package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SalimBlue,
    onPrimary = SalimWhite,
    primaryContainer = SalimSecondarySurfaceDark,
    onPrimaryContainer = SalimTextPrimaryDark,
    secondary = SalimGray,
    onSecondary = SalimWhite,
    background = SalimBackgroundDark,
    onBackground = SalimTextPrimaryDark,
    surface = SalimSurfaceDark,
    onSurface = SalimTextPrimaryDark,
    surfaceVariant = SalimSecondarySurfaceDark,
    onSurfaceVariant = SalimTextSecondaryDark,
    outline = SalimDividerDark,
    error = SalimRed,
    onError = SalimWhite
)

private val LightColorScheme = lightColorScheme(
    primary = SalimBlue,
    onPrimary = SalimWhite,
    primaryContainer = SalimSecondarySurfaceLight,
    onPrimaryContainer = SalimTextPrimaryLight,
    secondary = SalimGray,
    onSecondary = SalimWhite,
    background = SalimBackgroundLight,
    onBackground = SalimTextPrimaryLight,
    surface = SalimSurfaceLight,
    onSurface = SalimTextPrimaryLight,
    surfaceVariant = SalimSecondarySurfaceLight,
    onSurfaceVariant = SalimTextSecondaryLight,
    outline = SalimDividerLight,
    error = SalimRed,
    onError = SalimWhite
)

@Composable
fun SalimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
