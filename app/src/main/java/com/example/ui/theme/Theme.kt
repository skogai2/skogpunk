package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.example.model.Cabinet

private val SkogDarkColorScheme = darkColorScheme(
    primary = SkNeonViolet,
    onPrimary = SkFgOnNeon,
    primaryContainer = SkCard,
    onPrimaryContainer = SkFg,
    secondary = SkNeonCyan,
    onSecondary = SkFgOnNeon,
    tertiary = SkNeonMagenta,
    background = SkBg,
    onBackground = SkFg,
    surface = SkCard,
    onSurface = SkFg,
    surfaceVariant = SkShell,
    onSurfaceVariant = SkFg2,
    outline = SkBorder,
    outlineVariant = SkBorderSoft
)

@Composable
fun SkogaiRPGTheme(
    activeCabinet: Cabinet = Cabinet.SUDO_HERO,
    content: @Composable () -> Unit
) {
    val dynamicColors = SkogDarkColorScheme.copy(
        primary = activeCabinet.primaryColor,
        secondary = activeCabinet.accentColor,
        background = activeCabinet.surfaceColor.copy(alpha = 0.95f),
        surface = activeCabinet.surfaceColor,
        outline = activeCabinet.borderColor
    )

    MaterialTheme(
        colorScheme = dynamicColors,
        typography = SkogTypography,
        content = content
    )
}
