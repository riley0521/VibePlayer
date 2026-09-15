package com.rfcoding.vibeplayer.core.presentation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Figma keeps the tablet sheet a centered 480dp card instead of letting it span the screen. */
private val TabletSheetWidth = 480.dp

/** The corners Figma draws on every sheet; the bottom edge stays square against the screen edge. */
val VibeBottomSheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)

/**
 * The bottom sheet shell every VibePlayer sheet shares: the playlist name sheet, the playlist action
 * sheet and its delete confirmation. Figma draws the delete confirmation as a sheet too, not a dialog.
 *
 * It lives in `:core:presentation` rather than the design system because it needs
 * [currentDeviceConfiguration], and `:core:presentation` is what depends on `:core:design-system`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val isMobile = currentDeviceConfiguration().isMobile
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        // Unspecified lets the sheet fill a phone; Material's default would cap it at 640dp.
        sheetMaxWidth = if (isMobile) Dp.Unspecified else TabletSheetWidth,
        shape = VibeBottomSheetShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        scrimColor = MaterialTheme.colorScheme.scrim,
        // Figma draws no drag handle.
        dragHandle = null,
        content = content,
    )
}
