package com.rfcoding.vibeplayer.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VibePlayerColorScheme = darkColorScheme(
    primary = ButtonPrimary,
    onPrimary = TextPrimary,
    tertiary = Accent,
    onTertiary = SurfaceBg,
    background = SurfaceBg,
    onBackground = TextPrimary,
    surface = SurfaceBg,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHigher,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = SurfaceBg,
    surfaceContainerLow = SurfaceBg,
    surfaceContainer = SurfaceBg,
    surfaceContainerHigh = SurfaceHigher,
    surfaceContainerHighest = SurfaceHighest,
    outline = SurfaceOutline,
    outlineVariant = SurfaceOutline,
    scrim = SurfaceOverlay,
)

/**
 * VibePlayer only has the dark Figma palette, so neither the system theme nor dynamic color changes it.
 */
@Composable
fun VibePlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VibePlayerColorScheme,
        typography = VibeTypography,
        content = content,
    )
}
