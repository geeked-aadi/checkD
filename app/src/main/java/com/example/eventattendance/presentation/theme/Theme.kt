package com.example.eventattendance.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.eventattendance.domain.model.ThemeMode

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
    outlineVariant = Color(0xFFE4E4E7),
    inverseSurface = Gray800,
    inverseOnSurface = MonochromeWhite,
    error = StatusRed,
    errorContainer = StatusRedBg,
    onError = MonochromeWhite,
    onErrorContainer = Color(0xFF991B1B)
)

private val DarkColorScheme = darkColorScheme(
    primary = MonochromeWhite,
    onPrimary = MonochromeBlack,
    primaryContainer = Gray800,
    onPrimaryContainer = MonochromeWhite,
    secondary = Gray400,
    onSecondary = MonochromeBlack,
    background = Color(0xFF0F0F12),
    onBackground = MonochromeWhite,
    surface = Color(0xFF18181C),
    onSurface = MonochromeWhite,
    surfaceVariant = Color(0xFF26262B),
    onSurfaceVariant = Gray400,
    outline = Color(0xFF3A3A42),
    outlineVariant = Color(0xFF2E2E35),
    inverseSurface = Gray800,
    inverseOnSurface = MonochromeWhite,
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF450A0A),
    onError = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFEE2E2)
)

val ColorScheme.cardSurface: Color
    @Composable get() = surface

val ColorScheme.cardBorder: Color
    @Composable get() = outline

@Composable
fun CheckdTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    val statusColors = if (isDarkTheme) DarkStatusColors else LightStatusColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    CompositionLocalProvider(
        LocalCheckdStatusColors provides statusColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun CheckdTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    CheckdTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        content = content
    )
}

