package com.pramod.chessmasteroffline.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0E6B63),
    onPrimary = Color.White,
    secondary = Color(0xFF8A5A00),
    onSecondary = Color.White,
    tertiary = Color(0xFF7256A4),
    background = Color(0xFFF6F7F2),
    onBackground = Color(0xFF17201F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17201F),
    surfaceVariant = Color(0xFFE2E6DE),
    onSurfaceVariant = Color(0xFF414A47),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF6FD2C8),
    onPrimary = Color(0xFF003D38),
    secondary = Color(0xFFE9C16C),
    onSecondary = Color(0xFF442C00),
    tertiary = Color(0xFFC8B7F0),
    background = Color(0xFF101817),
    onBackground = Color(0xFFE8ECE7),
    surface = Color(0xFF17211F),
    onSurface = Color(0xFFE8ECE7),
    surfaceVariant = Color(0xFF35413E),
    onSurfaceVariant = Color(0xFFC2CBC6),
)

@Composable
fun ChessMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
