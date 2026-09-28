package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


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
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = ExactObsidianColorScheme, typography = Typography, content = content)
}

