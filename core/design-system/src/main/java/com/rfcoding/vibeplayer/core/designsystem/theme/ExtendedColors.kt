package com.rfcoding.vibeplayer.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/**
 * Figma color tokens that have no slot in Material 3's `ColorScheme`.
 */
@Stable
data class ExtendedColors(
    /** Button/Primary-30: border of a selected filter chip. */
    val buttonPrimary30: Color,
    /** Button/Hover: pressed overlay, icon-button and input backgrounds, disabled Filled button. */
    val buttonHover: Color,
    /** Text/Disabled */
    val textDisabled: Color,
    /** Surface/On-Surface: track of the mini player's progress bar. */
    val onSurfaceOverlay: Color,
)

internal val DarkExtendedColors = ExtendedColors(
    buttonPrimary30 = ButtonPrimary30,
    buttonHover = ButtonHover,
    textDisabled = TextDisabled,
    onSurfaceOverlay = OnSurfaceOverlay,
)

// VibePlayer has a single dark palette, so there is no CompositionLocal to switch between palettes.
val MaterialTheme.extendedColors: ExtendedColors
    get() = DarkExtendedColors
