package com.netoolhunter.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NetoolColors = darkColorScheme(
    primary = KaliBlue,
    onPrimary = TextPrimary,
    primaryContainer = KaliBlueDark,
    onPrimaryContainer = TextPrimary,
    secondary = KaliBlue,
    onSecondary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = ErrorColor,
    onError = TextPrimary,
    outline = BorderColor,
    outlineVariant = BorderColor
)

@Composable
fun NetoolHunterTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dark forced — light theme is intentionally not supported.
    MaterialTheme(
        colorScheme = NetoolColors,
        typography = NetoolHunterTypography,
        content = content
    )
}
