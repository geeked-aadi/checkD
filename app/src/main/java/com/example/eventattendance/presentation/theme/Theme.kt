package com.example.eventattendance.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = MonochromeBlack,
    onPrimary = MonochromeWhite,
    primaryContainer = Gray100,
    onPrimaryContainer = MonochromeBlack,
    secondary = Gray700,
    onSecondary = MonochromeWhite,
    background = MonochromeWhite,
    onBackground = MonochromeBlack,
    surface = MonochromeWhite,
    onSurface = MonochromeBlack,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = MonochromeCardBorder,
    error = StatusRed,
    errorContainer = StatusRedBg
)

private val DarkColorScheme = darkColorScheme(
    primary = MonochromeWhite,
    onPrimary = MonochromeBlack,
    primaryContainer = Gray800,
    onPrimaryContainer = MonochromeWhite,
    secondary = Gray400,
    onSecondary = MonochromeBlack,
    background = MonochromeBlack,
    onBackground = MonochromeWhite,
    surface = Gray900,
    onSurface = MonochromeWhite,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray400,
    outline = Gray700,
    error = StatusRed,
    errorContainer = Gray800
)

@Composable
fun CheckdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
