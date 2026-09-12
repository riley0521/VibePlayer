package com.rfcoding.vibeplayer.feature.permission.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.presentation.VibeBottomSheetShape

/**
 * Stands in for the sheet window the previews can't render, so the surface and corners still show.
 * `ModalBottomSheet` lives in its own window, which Compose previews don't draw.
 */
@Composable
internal fun SheetPreviewSurface(content: @Composable () -> Unit) {
    VibePlayerTheme {
        Box(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = VibeBottomSheetShape,
            ),
        ) {
            content()
        }
    }
}
