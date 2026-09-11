package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors

/**
 * The round icon button of the nav bars: a 44dp touch target around a 36dp circle with a 16dp glyph.
 */
@Composable
fun VibeIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(44.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.extendedColors.buttonHover)
                .indication(interactionSource, PressedOverlayIndication),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun VibeIconButtonPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            VibeIconButton(icon = VibeIcons.Scan, contentDescription = null, onClick = {})
            VibeIconButton(icon = VibeIcons.Search, contentDescription = null, onClick = {})
            VibeIconButton(icon = VibeIcons.ArrowLeft, contentDescription = null, onClick = {})
            VibeIconButton(
                icon = VibeIcons.HeartDuotone,
                contentDescription = null,
                onClick = {},
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
