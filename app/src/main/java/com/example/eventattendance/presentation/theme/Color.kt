package com.example.eventattendance.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Monochrome Primary Palette matching Reference Mockup
val MonochromeBlack = Color(0xFF141416)
val MonochromeWhite = Color(0xFFFFFFFF)
val MonochromeBackground = Color(0xFFFAFAFA)
val MonochromeCardBorder = Color(0xFFE4E4E7)
val MonochromeCardSurface = Color(0xFFFFFFFF)

// Neutrals & Surfaces
val Gray100 = Color(0xFFF4F4F5)
val Gray200 = Color(0xFFE4E4E7)
val Gray400 = Color(0xFFA1A1AA)
val Gray500 = Color(0xFF71717A)
val Gray600 = Color(0xFF52525B)
val Gray700 = Color(0xFF3F3F46)
val Gray800 = Color(0xFF27272A)
val Gray900 = Color(0xFF18181B)

// Accent / Status Indicators (Light Mode Base - Yellow adjusted for WCAG 4.5:1 on light container)
val StatusGreen = Color(0xFF16A34A)
val StatusGreenBg = Color(0xFFDCFCE7)
val StatusYellow = Color(0xFF854D0E)
val StatusYellowBg = Color(0xFFFEF9C3)
val StatusRed = Color(0xFFDC2626)
val StatusRedBg = Color(0xFFFEE2E2)

// Scanner Colors (Fixed on black camera viewfinder)
val ScannerBackground = Color(0xFF000000)
val ScannerOverlayStroke = Color(0xFFFFFFFF)
val ScannerLaserColor = Color(0xFFF4F4F5)

/**
 * Theme-aware semantic status colors for success, warning, and danger states.
 * Guarantees WCAG AA contrast in both light and dark themes.
 */
@Immutable
data class CheckdStatusColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val danger: Color,
    val onDanger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
)

val LightStatusColors = CheckdStatusColors(
    success = Color(0xFF16A34A),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFDCFCE7),
    onSuccessContainer = Color(0xFF14532D),
    warning = Color(0xFF854D0E),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFEF9C3),
    onWarningContainer = Color(0xFF854D0E),
    danger = Color(0xFFDC2626),
    onDanger = Color(0xFFFFFFFF),
    dangerContainer = Color(0xFFFEE2E2),
    onDangerContainer = Color(0xFF991B1B),
)

val DarkStatusColors = CheckdStatusColors(
    success = Color(0xFF4ADE80),
    onSuccess = Color(0xFF052E16),
    successContainer = Color(0xFF14532D),
    onSuccessContainer = Color(0xFFDCFCE7),
    warning = Color(0xFFFACC15),
    onWarning = Color(0xFF422006),
    warningContainer = Color(0xFF422006),
    onWarningContainer = Color(0xFFFEF9C3),
    danger = Color(0xFFF87171),
    onDanger = Color(0xFF450A0A),
    dangerContainer = Color(0xFF450A0A),
    onDangerContainer = Color(0xFFFEE2E2),
)

val LocalCheckdStatusColors = staticCompositionLocalOf { LightStatusColors }

val MaterialTheme.statusColors: CheckdStatusColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCheckdStatusColors.current

