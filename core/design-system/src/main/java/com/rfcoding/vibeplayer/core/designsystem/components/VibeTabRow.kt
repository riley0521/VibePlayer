package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyMediumMedium

/**
 * Figma "tab-bar": equal-width text tabs over a divider, the selected one underlined.
 */
@Composable
fun VibeTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .bottomBorder(MaterialTheme.colorScheme.outline)
            .padding(top = 4.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEachIndexed { index, title ->
            VibeTab(
                title = title,
                selected = index == selectedTabIndex,
                onClick = { onTabClick(index) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun VibeTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .height(40.dp)
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = PressedOverlayIndication,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // The indicator spans the label's width, so it's drawn on the label's wrapper.
        Box(
            modifier = Modifier
                .then(if (selected) Modifier.tabIndicator(colorScheme.onSurface) else Modifier)
                .padding(bottom = 12.dp),
        ) {
            Text(
                text = title,
                style = if (selected) {
                    MaterialTheme.typography.bodyMediumMedium
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = if (selected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 2dp bar along the bottom edge, inset 2dp on each side, with fully rounded top corners. */
private fun Modifier.tabIndicator(color: Color): Modifier = drawWithCache {
    val height = 2.dp.toPx()
    val inset = 2.dp.toPx()
    val indicator = Path().apply {
        addRoundRect(
            RoundRect(
                left = inset,
                top = size.height - height,
                right = size.width - inset,
                bottom = size.height,
                topLeftCornerRadius = CornerRadius(height / 2),
                topRightCornerRadius = CornerRadius(height / 2),
            ),
        )
    }
    onDrawBehind { drawPath(indicator, color) }
}

@Preview
@Composable
private fun VibeTabRowPreview() {
    PreviewSurface {
        VibeTabRow(tabs = listOf("Songs", "Playlists"), selectedTabIndex = 0, onTabClick = {})
        VibeTabRow(tabs = listOf("Songs", "Playlists"), selectedTabIndex = 1, onTabClick = {})
    }
}
