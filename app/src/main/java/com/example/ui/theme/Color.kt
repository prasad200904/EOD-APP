package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Exact Dark Obsidian Theme Palette from the Screenshots
val EodDarkBackground = Color(0xFF0F0F12)
val EodDarkSurface = Color(0xFF191A20)
val EodDarkSurfaceElevated = Color(0xFF202128)
val EodDarkCardBorder = Color(0xFF262730)
val EodDarkInputBg = Color(0xFF141519)
val EodDarkInputBorder = Color(0xFF2B2C36)

val EodPrimaryPurple = Color(0xFF5348DE) // Exact "Submit EOD" / "Generate report" purple
val EodPrimaryPurpleHover = Color(0xFF6459E8)
val EodActivePillBg = Color(0xFFF1F0FE)
val EodActivePillText = Color(0xFF3F32C0)

val EodTextPrimary = Color(0xFFFFFFFF)
val EodTextSecondary = Color(0xFF8E909D)
val EodTextMuted = Color(0xFF636573)

// Status Pill Colors
val EodDonePillBg = Color(0xFFD0F4EB)
val EodDonePillText = Color(0xFF0A4F41)

val EodInProgressPillBg = Color(0xFFFFE8D6)
val EodInProgressPillText = Color(0xFF7C3E08)

val EodMissedPillBg = Color(0xFFFFE4E6)
val EodMissedPillText = Color(0xFF9F1239)

val EodAvatarBg = Color(0xFF0D2847)
val EodAvatarText = Color(0xFF38BDF8)

// Backward compatibility tokens
val WorkPrimary = EodPrimaryPurple
val WorkOnPrimary = Color(0xFFFFFFFF)
val WorkPrimaryContainer = EodPrimaryPurple
val WorkOnPrimaryContainer = Color(0xFFFFFFFF)

val WorkBackground = EodDarkBackground
val WorkOnBackground = Color(0xFFFFFFFF)
val WorkSurface = EodDarkSurface
val WorkOnSurface = Color(0xFFFFFFFF)
val WorkSurfaceVariant = EodDarkSurfaceElevated
val WorkOnSurfaceVariant = EodTextSecondary
val WorkOutline = EodDarkInputBorder
val WorkOutlineVariant = EodDarkCardBorder

val WorkSecondary = Color(0xFF8E909D)
val WorkOnSecondary = Color(0xFFFFFFFF)
val WorkSecondaryContainer = EodDarkSurfaceElevated
val WorkOnSecondaryContainer = Color(0xFFFFFFFF)

val WorkTertiary = Color(0xFF38BDF8)
val WorkOnTertiary = Color(0xFFFFFFFF)
val WorkTertiaryContainer = Color(0xFF0D2847)
val WorkOnTertiaryContainer = Color(0xFF38BDF8)

val WorkError = Color(0xFFF43F5E)
val WorkOnError = Color(0xFFFFFFFF)
val WorkErrorContainer = EodMissedPillBg
val WorkOnErrorContainer = EodMissedPillText

val StatusSuccessBg = EodDonePillBg
val StatusSuccessText = EodDonePillText
val StatusInfoBg = Color(0xFFD0E8FF)
val StatusInfoText = Color(0xFF0C4A6E)
val StatusWarningBg = EodInProgressPillBg
val StatusWarningText = EodInProgressPillText
val StatusErrorBg = EodMissedPillBg
val StatusErrorText = EodMissedPillText
val StatusPendingBg = Color(0xFFE0E7FF)
val StatusPendingText = Color(0xFF3730A3)


