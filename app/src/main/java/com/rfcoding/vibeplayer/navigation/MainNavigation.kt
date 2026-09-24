package com.rfcoding.vibeplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rfcoding.vibeplayer.R
import com.rfcoding.vibeplayer.core.designsystem.components.VibeNavigationBar
import com.rfcoding.vibeplayer.core.designsystem.components.VibeNavigationItem
import com.rfcoding.vibeplayer.core.designsystem.components.VibeNavigationRail
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration

/** The app's top-level screens. Each lives in its own feature, so switching goes through `:app`. */
enum class MainTab {
    Library,
    Downloader,
}

/** A bottom bar on mobile and a start-edge rail on tablet; the screens place it accordingly. */
@Composable
fun MainNavigation(
    selectedTab: MainTab,
    onTabClick: (MainTab) -> Unit,
) {
    val items = listOf(
        VibeNavigationItem(label = stringResource(R.string.tab_library), icon = VibeIcons.Music),
        VibeNavigationItem(label = stringResource(R.string.tab_downloader), icon = VibeIcons.Download),
    )
    val onItemClick = { index: Int ->
        val tab = MainTab.entries[index]
        if (tab != selectedTab) onTabClick(tab)
    }
    if (currentDeviceConfiguration().isMobile) {
        VibeNavigationBar(items = items, selectedIndex = selectedTab.ordinal, onItemClick = onItemClick)
    } else {
        VibeNavigationRail(items = items, selectedIndex = selectedTab.ordinal, onItemClick = onItemClick)
    }
}
