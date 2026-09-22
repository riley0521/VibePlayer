package com.rfcoding.vibeplayer.core.designsystem.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.rfcoding.vibeplayer.core.designsystem.R

/**
 * The Figma icon set. Every icon is a single-tint 24dp vector: pass the color through `Icon(tint = …)`.
 * An icon that Figma draws in more than one style has the style as a suffix.
 */
object VibeIcons {
    val Search: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_search)
    val Scan: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_scan)
    val ArrowLeft: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_arrow_left)
    val ArrowUp: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_arrow_up)
    val ChevronDown: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_chevron_down)
    /** Drawn for 16dp, as in the search field. */
    val Close: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_x)

    val PlayFilled: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_filled_play)
    val PlayLinear: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_play)
    val Pause: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_filled_pause)
    val SkipNext: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_filled_skip_next)
    val SkipPrevious: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_filled_skip_previous)
    val Repeat: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_repeat)
    val RepeatOne: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_repeat_one)
    val RepeatOff: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_repeat_off)
    val Shuffle: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_shuffle)
    val Queue: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_queue)
    val Timer: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_timer)

    val Plus: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_plus)
    val Check: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_check)
    val MenuDots: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_menu_dots)
    /** The Edit playlist screen's drag handle. */
    val Menu: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_outline_menu)
    val Bin: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_bin)
    val Pen: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_pen)
    val ImageEdit: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_img_edit)
    val Download: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_outline_download)

    val Music: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_duotone_music)
    val HeartLinear: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_heart)
    val HeartDuotone: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_duotone_heart)
    val PlaylistLinear: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_linear_playlist)
    val PlaylistDuotone: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_duotone_playlist)
}
