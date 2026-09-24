package com.rfcoding.vibeplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.feature.downloader.presentation.DownloaderGraph
import com.rfcoding.vibeplayer.feature.downloader.presentation.DownloaderRoute
import com.rfcoding.vibeplayer.feature.downloader.presentation.downloaderGraph
import com.rfcoding.vibeplayer.feature.library.presentation.LibraryGraph
import com.rfcoding.vibeplayer.feature.library.presentation.LibraryRoute
import com.rfcoding.vibeplayer.feature.library.presentation.libraryGraph
import com.rfcoding.vibeplayer.feature.permission.presentation.PermissionGraph
import com.rfcoding.vibeplayer.feature.permission.presentation.permissionGraph
import com.rfcoding.vibeplayer.feature.player.presentation.PlayerGraph
import com.rfcoding.vibeplayer.feature.player.presentation.PlayerRoute
import com.rfcoding.vibeplayer.feature.player.presentation.playerGraph
import kotlinx.coroutines.flow.Flow

/**
 * @param openPlayerRequests emits when the media notification is tapped, so the Player opens on top of
 * Library.
 */
@Composable
fun NavigationRoot(
    startDestination: Any,
    openPlayerRequests: Flow<Unit>,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        permissionGraph(
            onPermissionGranted = {
                navController.navigate(LibraryGraph) {
                    // The permission screen must never be reachable again once access is granted.
                    popUpTo<PermissionGraph> { inclusive = true }
                }
            },
        )
        libraryGraph(
            navController = navController,
            onOpenPlayer = { navController.openPlayer() },
            navigation = {
                MainNavigation(selectedTab = MainTab.Library, onTabClick = navController::openTab)
            },
        )
        downloaderGraph(
            onOpenPlayer = { navController.openPlayer() },
            navigation = {
                MainNavigation(selectedTab = MainTab.Downloader, onTabClick = navController::openTab)
            },
        )
        playerGraph(
            onNavigateBack = { navController.navigateUp() },
        )
    }

    // After the NavHost, so its graph is set by the time a cold-start request is collected.
    ObserveAsEvents(openPlayerRequests) { navController.openPlayer() }
}

/**
 * Leaves the back stack as exactly [Library, Player] or [Library, Downloader, Player], whether it's
 * opened from the mini player, a search result or the notification: anything above the tab the user
 * is on (Search, a Playlist Page, …) is dropped, so Back from the Player lands on that tab.
 */
private fun NavController.openPlayer() {
    val backStack = currentBackStack.value
    // Without Library on the stack the permission screen is showing, so there is nothing to play.
    if (backStack.none { it.destination.hasRoute<LibraryRoute>() }) return
    if (currentDestination?.hasRoute<PlayerRoute>() == true) return
    val isOnDownloader = backStack.any { it.destination.hasRoute<DownloaderRoute>() }
    navigate(PlayerGraph) {
        if (isOnDownloader) popUpTo<DownloaderRoute>() else popUpTo<LibraryRoute>()
        launchSingleTop = true
    }
}

/**
 * Library is the home tab: Downloader sits on top of it, so Back from Downloader returns to
 * Library. Each tab's state (its list scroll, the pasted link) is saved while the other shows.
 */
private fun NavController.openTab(tab: MainTab) {
    when (tab) {
        MainTab.Library -> popBackStack<LibraryRoute>(inclusive = false, saveState = true)
        MainTab.Downloader -> navigate(DownloaderGraph) {
            popUpTo<LibraryRoute> { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}
