package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.R
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors

/**
 * Figma "radio-buttons". Pass a null [onClick] when a parent (e.g. a whole chip) handles the selection.
 */
@Composable
fun VibeRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val tint = when {
        !enabled -> MaterialTheme.extendedColors.textDisabled
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.selectable(
                        selected = selected,
                        interactionSource = null,
                        indication = PressedOverlayIndication,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(if (selected) R.drawable.ic_radio_selected else R.drawable.ic_radio_unselected),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Preview
@Composable
private fun VibeRadioButtonPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VibeRadioButton(selected = true, onClick = {})
            VibeRadioButton(selected = false, onClick = {})
            VibeRadioButton(selected = true, onClick = {}, enabled = false)
            VibeRadioButton(selected = false, onClick = {}, enabled = false)
        }
    }
}
