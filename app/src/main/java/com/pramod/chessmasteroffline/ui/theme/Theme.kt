package com.pramod.chessmasteroffline.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pramod.chessmasteroffline.R

val HudBackground = Color(0xFF040810)
val HudSurface = Color(0xFF080F1E)
val HudElevated = Color(0xFF0E2444)
val HudBlue = Color(0xFF00E5FF)
val HudViolet = Color(0xFFB388FF)
val HudAmber = HudViolet
val HudGreen = HudBlue
val HudText = Color(0xFFE2F0FF)
val HudMuted = Color(0x73B4D2FF)
val HudBorder = Color(0xFF0D2240)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
)

private val HudScheme = darkColorScheme(
    primary = HudBlue,
    onPrimary = HudBackground,
    secondary = HudAmber,
    onSecondary = HudBackground,
    tertiary = HudViolet,
    onTertiary = HudText,
    background = HudBackground,
    onBackground = HudText,
    surface = HudSurface,
    onSurface = HudText,
    surfaceVariant = HudElevated,
    onSurfaceVariant = HudMuted,
    outline = HudBorder,
    error = HudViolet,
    onError = HudBackground,
)

private val HudTextStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Normal,
    color = HudText,
)

private val HudTypography = Typography(
    displayLarge = HudTextStyle.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    headlineLarge = HudTextStyle.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    headlineMedium = HudTextStyle.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    headlineSmall = HudTextStyle.copy(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    titleLarge = HudTextStyle.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    titleMedium = HudTextStyle.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    titleSmall = HudTextStyle.copy(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
    bodyLarge = HudTextStyle.copy(fontSize = 13.sp, lineHeight = 18.sp),
    bodyMedium = HudTextStyle.copy(fontSize = 12.sp, lineHeight = 17.sp),
    bodySmall = HudTextStyle.copy(fontSize = 10.sp, lineHeight = 14.sp),
    labelLarge = HudTextStyle.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelMedium = HudTextStyle.copy(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
    labelSmall = HudTextStyle.copy(fontSize = 9.sp, lineHeight = 12.sp),
)

@Composable
fun ChessMasterTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = HudScheme,
        typography = HudTypography,
        content = content,
    )
}
