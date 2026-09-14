package com.rfcoding.vibeplayer.feature.library.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.rfcoding.vibeplayer.feature.library.presentation.addsongs.AddSongsRoot
import com.rfcoding.vibeplayer.feature.library.presentation.library.LibraryRoot
import com.rfcoding.vibeplayer.feature.library.presentation.scan.ScanMusicRoot
import kotlinx.serialization.Serializable

@Serializable
data object LibraryGraph

@Serializable
data object LibraryRoute

@Serializable
data object ScanMusicRoute

@Serializable
data class AddSongsRoute(val playlistId: Long)

/**
 * Library → Scan music and Library → Add songs stay inside this feature, so the graph navigates on its
 * own. Opening the player crosses into another feature, so `:app` handles [onMiniPlayerClick].
 */
fun NavGraphBuilder.libraryGraph(
    navController: NavController,
    onMiniPlayerClick: () -> Unit,
) {
    navigation<LibraryGraph>(startDestination = LibraryRoute) {
        composable<LibraryRoute> {
            LibraryRoot(
                onScanClick = { navController.navigate(ScanMusicRoute) },
                onPlaylistCreated = { playlistId -> navController.navigate(AddSongsRoute(playlistId)) },
                onMiniPlayerClick = onMiniPlayerClick,
            )
        }
        composable<ScanMusicRoute> {
            ScanMusicRoot(
                onNavigateBack = { navController.navigateUp() },
            )
        }
        composable<AddSongsRoute> { backStackEntry ->
            AddSongsRoot(
                playlistId = backStackEntry.toRoute<AddSongsRoute>().playlistId,
                onNavigateBack = { navController.navigateUp() },
            )
        }
    }
}
