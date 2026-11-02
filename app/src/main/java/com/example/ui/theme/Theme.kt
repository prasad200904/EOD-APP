package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


// NEW: Warm Gradient Light Theme
private val WarmGradientLightScheme =
  lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralLight,
    onPrimaryContainer = DarkSlateDeep,
    secondary = DarkSlate,
    onSecondary = Color.White,
    secondaryContainer = MintGreen,
    onSecondaryContainer = DarkSlateDeep,
    tertiary = WarmBlue,
    onTertiary = Color.White,
    tertiaryContainer = WarmCream,
    onTertiaryContainer = DarkSlateDeep,
    background = WarmCream,
    onBackground = DarkSlateDeep,
    surface = Color.White,
    onSurface = DarkSlateDeep,
    surfaceVariant = WarmCream,
    onSurfaceVariant = SubtleText,
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFFDC2626),
  )

// Legacy: Keep dark theme for backward compatibility
private val ExactObsidianColorScheme =
  darkColorScheme(
    primary = EodPrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = EodPrimaryPurple,
    onPrimaryContainer = Color.White,
    secondary = EodTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = EodDarkSurfaceElevated,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF0D2847),
    onTertiaryContainer = Color(0xFF38BDF8),
    background = EodDarkBackground,
    onBackground = EodTextPrimary,
    surface = EodDarkSurface,
    onSurface = EodTextPrimary,
    surfaceVariant = EodDarkSurfaceElevated,
    onSurfaceVariant = EodTextSecondary,
    outline = EodDarkInputBorder,
    outlineVariant = EodDarkCardBorder,
    error = WorkError,
    onError = Color.White,
    errorContainer = EodMissedPillBg,
    onErrorContainer = EodMissedPillText,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Changed default to light theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Use warm gradient light theme by default
  MaterialTheme(colorScheme = WarmGradientLightScheme, typography = Typography, content = content)
}

