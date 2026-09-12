package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyLargeMedium
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors

enum class VibeButtonStyle { Filled, Outlined, Text, Destructive }

/**
 * Figma "Button". [isLoading] shows the loader before the label and makes the button non-clickable.
 */
@Composable
fun VibeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: VibeButtonStyle = VibeButtonStyle.Filled,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = MaterialTheme.extendedColors
    val isClickable = enabled && !isLoading
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val contentColor = when {
        !isClickable -> extendedColors.textDisabled
        style == VibeButtonStyle.Filled || style == VibeButtonStyle.Destructive -> colorScheme.onPrimary
        style == VibeButtonStyle.Outlined -> colorScheme.onSurface
        else -> colorScheme.primary
    }
    val containerModifier = when (style) {
        // Figma gives the destructive button the same purple glow as the primary one, so both share it.
        VibeButtonStyle.Filled, VibeButtonStyle.Destructive -> Modifier
            .then(if (isClickable && !isPressed) Modifier.primaryDropShadow(CircleShape) else Modifier)
            .background(
                color = when {
                    !isClickable -> extendedColors.buttonHover
                    style == VibeButtonStyle.Destructive -> extendedColors.buttonDestructive
                    else -> colorScheme.primary
                },
                shape = CircleShape,
            )
            .height(44.dp)
        VibeButtonStyle.Outlined -> Modifier
            .border(width = 1.dp, color = colorScheme.outline, shape = CircleShape)
            .height(44.dp)
        VibeButtonStyle.Text -> Modifier
    }
    val contentPadding = when (style) {
        VibeButtonStyle.Text -> PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        else -> PaddingValues(horizontal = 24.dp, vertical = 10.dp)
    }

    Row(
        modifier = modifier
            .then(containerModifier)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = PressedOverlayIndication,
                enabled = isClickable,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoading) {
            VibeLoader(size = 16.dp, tint = contentColor)
        } else if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLargeMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun VibeButtonPreview() {
    PreviewSurface {
        VibeButton(text = "Allow Access", onClick = {}, modifier = Modifier.fillMaxWidth())
        VibeButton(text = "Create", onClick = {}, enabled = false)
        VibeButton(text = "Scanning", onClick = {}, isLoading = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VibeButton(
                text = "Shuffle",
                onClick = {},
                style = VibeButtonStyle.Outlined,
                leadingIcon = VibeIcons.Shuffle,
                modifier = Modifier.weight(1f),
            )
            VibeButton(
                text = "Play",
                onClick = {},
                style = VibeButtonStyle.Outlined,
                leadingIcon = VibeIcons.PlayLinear,
                modifier = Modifier.weight(1f),
            )
        }
        VibeButton(text = "Cancel", onClick = {}, style = VibeButtonStyle.Outlined, enabled = false)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VibeButton(
                text = "Cancel",
                onClick = {},
                style = VibeButtonStyle.Outlined,
                modifier = Modifier.weight(1f),
            )
            VibeButton(
                text = "Delete",
                onClick = {},
                style = VibeButtonStyle.Destructive,
                modifier = Modifier.weight(1f),
            )
        }
        VibeButton(text = "Cancel", onClick = {}, style = VibeButtonStyle.Text)
        VibeButton(text = "Cancel", onClick = {}, style = VibeButtonStyle.Text, enabled = false)
    }
}
