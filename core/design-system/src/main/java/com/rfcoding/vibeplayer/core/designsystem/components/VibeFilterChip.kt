package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyLargeMedium
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors

/**
 * Figma's scan filter pill: a [VibeRadioButton] and its label inside a bordered, fully rounded row.
 * The whole chip is the selectable target, so the radio button itself takes no click.
 */
@Composable
fun VibeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(44.dp)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.extendedColors.buttonPrimary30 else colorScheme.outline,
                shape = CircleShape,
            )
            .clip(CircleShape)
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = PressedOverlayIndication,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VibeRadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = if (selected) {
                MaterialTheme.typography.bodyLargeMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun VibeFilterChipPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VibeFilterChip(label = "30s", selected = true, onClick = {}, modifier = Modifier.weight(1f))
            VibeFilterChip(label = "60s", selected = false, onClick = {}, modifier = Modifier.weight(1f))
        }
    }
}
