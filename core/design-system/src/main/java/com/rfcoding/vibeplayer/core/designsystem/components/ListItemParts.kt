package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** The row shared by the song and playlist cards: pressable, 12dp vertical padding, divider below. */
@Composable
internal fun ListItemRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = PressedOverlayIndication,
                onClick = onClick,
            )
            .bottomBorder(MaterialTheme.colorScheme.outline)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** Artwork fallback: [icon] in [tint] over a faint vertical gradient of the same color. */
@Composable
internal fun ArtworkPlaceholder(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    iconFraction: Float = 36f / 64f,
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(tint.copy(alpha = 0.14f), tint.copy(alpha = 0.03f))),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.fillMaxSize(iconFraction),
        )
    }
}
