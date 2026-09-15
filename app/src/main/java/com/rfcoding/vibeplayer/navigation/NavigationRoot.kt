package com.rfcoding.vibeplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
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
        )
        playerGraph(
            onNavigateBack = { navController.navigateUp() },
        )
    }

    // After the NavHost, so its graph is set by the time a cold-start request is collected.
    ObserveAsEvents(openPlayerRequests) { navController.openPlayer() }
}

/**
 * Leaves the back stack as exactly [Library, Player], whether it's opened from the mini player, a
 * search result or the notification: anything above Library (Search, a Playlist Page, …) is dropped,
 * so Back from the Player always lands on Library.
 */
private fun NavController.openPlayer() {
    // Without Library on the stack the permission screen is showing, so there is nothing to play.
    if (currentBackStack.value.none { it.destination.hasRoute<LibraryRoute>() }) return
    if (currentDestination?.hasRoute<PlayerRoute>() == true) return
    navigate(PlayerGraph) {
        popUpTo<LibraryRoute>()
        launchSingleTop = true
    }
}
