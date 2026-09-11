package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Figma "loader", which is Material's indeterminate circular progress indicator with a stroke of 1/5 its size.
 */
@Composable
fun VibeLoader(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = tint,
        strokeWidth = size / 5,
        trackColor = Color.Transparent,
        strokeCap = StrokeCap.Round,
    )
}

@Preview
@Composable
private fun VibeLoaderPreview() {
    PreviewSurface {
        VibeLoader()
        VibeLoader(size = 16.dp)
    }
}
