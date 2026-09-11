package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.R
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors

private const val DisabledAlpha = 0.38f

/**
 * Figma "checkbox" (drawn as a circle). Pass a null [onCheckedChange] when a parent row handles the toggle.
 */
@Composable
fun VibeCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = MaterialTheme.extendedColors
    Box(
        modifier = modifier
            .size(28.dp)
            .alpha(if (enabled) 1f else DisabledAlpha)
            .clip(CircleShape)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(
                        value = checked,
                        interactionSource = null,
                        indication = PressedOverlayIndication,
                        enabled = enabled,
                        role = Role.Checkbox,
                        onValueChange = onCheckedChange,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = if (enabled) colorScheme.primary else extendedColors.textDisabled,
                        shape = CircleShape,
                    ),
            )
            Icon(
                painter = painterResource(R.drawable.ic_check_small),
                contentDescription = null,
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(
                        width = 1.dp,
                        color = if (enabled) colorScheme.onSurfaceVariant else extendedColors.textDisabled,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun VibeCheckboxPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VibeCheckbox(checked = true, onCheckedChange = {})
            VibeCheckbox(checked = false, onCheckedChange = {})
            VibeCheckbox(checked = true, onCheckedChange = {}, enabled = false)
            VibeCheckbox(checked = false, onCheckedChange = {}, enabled = false)
        }
    }
}
