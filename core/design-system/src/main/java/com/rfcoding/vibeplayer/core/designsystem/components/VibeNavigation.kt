package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyMediumMedium

/** Height of [VibeNavigationBar], not counting the navigation-bar inset below it. */
val NavigationBarHeight = 64.dp

private val NavigationRailWidth = 88.dp
private val ItemIconSize = 24.dp

@Immutable
data class VibeNavigationItem(
    val label: String,
    val icon: ImageVector,
)

/**
 * The mobile switch between the app's top-level screens: evenly shared items over a top divider.
 * It pads itself for the navigation bar, so it sits flush with the bottom edge.
 */
@Composable
fun VibeNavigationBar(
    items: List<VibeNavigationItem>,
    selectedIndex: Int,
    onItemClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .topBorder(MaterialTheme.colorScheme.outline)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .height(NavigationBarHeight)
            .padding(horizontal = 16.dp)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            NavigationItem(
                item = item,
                selected = index == selectedIndex,
                onClick = { onItemClick(index) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * The tablet form of [VibeNavigationBar]: a column of items at the top of a start-edge rail. It
 * pads itself for the status, navigation and cutout insets on its side.
 */
@Composable
fun VibeNavigationRail(
    items: List<VibeNavigationItem>,
    selectedIndex: Int,
    onItemClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .endBorder(MaterialTheme.colorScheme.outline)
            .windowInsetsPadding(
                WindowInsets.systemBars
                    .union(WindowInsets.displayCutout)
                    .only(WindowInsetsSides.Start + WindowInsetsSides.Vertical),
            )
            .width(NavigationRailWidth)
            .padding(vertical = 24.dp)
            .selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.forEachIndexed { index, item ->
            NavigationItem(
                item = item,
                selected = index == selectedIndex,
                onClick = { onItemClick(index) },
            )
        }
    }
}

@Composable
private fun NavigationItem(
    item: VibeNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = PressedOverlayIndication,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(ItemIconSize),
        )
        Text(
            text = item.label,
            style = if (selected) MaterialTheme.typography.bodyMediumMedium else MaterialTheme.typography.bodyMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun VibeNavigationBarPreview() {
    val items = listOf(
        VibeNavigationItem(label = "Library", icon = VibeIcons.Music),
        VibeNavigationItem(label = "Downloader", icon = VibeIcons.Download),
    )
    PreviewSurface {
        VibeNavigationBar(items = items, selectedIndex = 0, onItemClick = {})
        VibeNavigationBar(items = items, selectedIndex = 1, onItemClick = {})
    }
}

@Preview(heightDp = 300)
@Composable
private fun VibeNavigationRailPreview() {
    PreviewSurface {
        VibeNavigationRail(
            items = listOf(
                VibeNavigationItem(label = "Library", icon = VibeIcons.Music),
                VibeNavigationItem(label = "Downloader", icon = VibeIcons.Download),
            ),
            selectedIndex = 1,
            onItemClick = {},
        )
    }
}
