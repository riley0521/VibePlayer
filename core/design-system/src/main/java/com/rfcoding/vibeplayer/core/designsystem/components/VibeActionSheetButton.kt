package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyLargeMedium
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors

/**
 * Figma "action-sheet-button": a full-width row with a round icon and a label.
 */
@Composable
fun VibeActionSheetButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = null,
                indication = PressedOverlayIndication,
                role = Role.Button,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.extendedColors.buttonHover, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLargeMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun VibeActionSheetButtonPreview() {
    PreviewSurface {
        VibeActionSheetButton(text = "Scan Music", icon = VibeIcons.Scan, onClick = {})
        VibeActionSheetButton(text = "Rename", icon = VibeIcons.Pen, onClick = {})
        VibeActionSheetButton(text = "Delete", icon = VibeIcons.Bin, onClick = {})
    }
}
