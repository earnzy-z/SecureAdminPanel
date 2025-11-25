package com.earnzy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6C5CE7),
    primaryContainer = Color(0xFF5E3C7A),
    onPrimary = Color.White,
    secondary = Color(0xFF00D4FF),
    secondaryContainer = Color(0xFF00A3CC),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF6B6B),
    background = Color(0xFF0F1419),
    surface = Color(0xFF1A1F29),
    surfaceVariant = Color(0xFF2D3142),
    onSurfaceVariant = Color(0xFFB0B5C1),
    outline = Color(0xFF6B7280),
    error = Color(0xFFFF6B6B)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6C5CE7),
    primaryContainer = Color(0xFFEADFFF),
    onPrimary = Color.White,
    secondary = Color(0xFF00D4FF),
    secondaryContainer = Color(0xFFB3E5FC),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF6B6B),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFD1D5DB),
    error = Color(0xFFFF6B6B)
)

@Composable
fun EarnzyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = Typography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
