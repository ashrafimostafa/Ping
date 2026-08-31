package com.mostafa.ping.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PingColorScheme = lightColorScheme(
    primary = PingColors.Charcoal,
    onPrimary = PingColors.OffWhite,
    secondary = PingColors.Charcoal82,
    onSecondary = PingColors.OffWhite,
    background = PingColors.Cream,
    onBackground = PingColors.Charcoal,
    surface = PingColors.Cream,
    onSurface = PingColors.Charcoal,
    surfaceVariant = PingColors.Cream,
    onSurfaceVariant = PingColors.Muted,
    outline = PingColors.Border,
    outlineVariant = PingColors.Charcoal40,
    error = PingColors.Charcoal,
    onError = PingColors.OffWhite
)

@Composable
fun PingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PingColorScheme,
        typography = PingTypography,
        content = content
    )
}
