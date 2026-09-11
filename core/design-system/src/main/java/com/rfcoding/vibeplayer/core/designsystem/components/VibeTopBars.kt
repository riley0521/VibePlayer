package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.R
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyLargeMedium

/**
 * Figma nav-bar `main-screen`: the logo lockup on the left and [actions] (e.g. scan, search) on the right.
 */
@Composable
fun VibeMainTopBar(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .height(64.dp)
            .padding(start = 16.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VibeLogo(size = 24.dp)
            Text(
                text = stringResource(R.string.vibe_player),
                style = MaterialTheme.typography.bodyLargeMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

/**
 * Figma nav-bar `search`: the search field and a Cancel text button.
 */
@Composable
fun VibeSearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onCancelClick: () -> Unit,
    placeholder: String,
    cancelText: String,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .height(64.dp)
            .padding(start = 16.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VibeSearchField(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = placeholder,
            onClearClick = onClearClick,
            modifier = Modifier.weight(1f),
        )
        VibeButton(
            text = cancelText,
            onClick = onCancelClick,
            style = VibeButtonStyle.Text,
        )
    }
}

/**
 * Figma nav-bar `inner-page`: back button, centered title and up to two [actions].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeInnerTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector = VibeIcons.ArrowLeft,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLargeMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        // The app bar already pads its slots by 4dp; Figma's side padding is 10dp.
        modifier = modifier.padding(horizontal = 6.dp),
        navigationIcon = {
            VibeIconButton(
                icon = navigationIcon,
                contentDescription = stringResource(R.string.navigate_back),
                onClick = onBackClick,
            )
        },
        actions = actions,
        expandedHeight = 64.dp,
        windowInsets = windowInsets,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}

@Preview
@Composable
private fun VibeTopBarsPreview() {
    PreviewSurface {
        VibeMainTopBar(windowInsets = WindowInsets(0)) {
            VibeIconButton(icon = VibeIcons.Scan, contentDescription = null, onClick = {})
            VibeIconButton(icon = VibeIcons.Search, contentDescription = null, onClick = {})
        }
        VibeSearchTopBar(
            query = "",
            onQueryChange = {},
            onClearClick = {},
            onCancelClick = {},
            placeholder = "Search",
            cancelText = "Cancel",
            windowInsets = WindowInsets(0),
        )
        VibeSearchTopBar(
            query = "Lo-fi",
            onQueryChange = {},
            onClearClick = {},
            onCancelClick = {},
            placeholder = "Search",
            cancelText = "Cancel",
            windowInsets = WindowInsets(0),
        )
        VibeInnerTopBar(title = "Scan Music", onBackClick = {}, windowInsets = WindowInsets(0))
        VibeInnerTopBar(
            title = "Now Playing",
            onBackClick = {},
            navigationIcon = VibeIcons.ChevronDown,
            windowInsets = WindowInsets(0),
        ) {
            VibeIconButton(icon = VibeIcons.PlaylistLinear, contentDescription = null, onClick = {})
            VibeIconButton(icon = VibeIcons.HeartLinear, contentDescription = null, onClick = {})
        }
    }
}
