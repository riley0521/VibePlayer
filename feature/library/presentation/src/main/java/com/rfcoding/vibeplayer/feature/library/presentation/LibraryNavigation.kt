package com.rfcoding.vibeplayer.feature.library.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.rfcoding.vibeplayer.feature.library.presentation.addsongs.AddSongsRoot
import com.rfcoding.vibeplayer.feature.library.presentation.editplaylist.EditPlaylistRoot
import com.rfcoding.vibeplayer.feature.library.presentation.library.LibraryRoot
import com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail.PlaylistDetailRoot
import com.rfcoding.vibeplayer.feature.library.presentation.scan.ScanMusicRoot
import com.rfcoding.vibeplayer.feature.library.presentation.search.SearchRoot
import kotlinx.serialization.Serializable

@Serializable
data object LibraryGraph

@Serializable
data object LibraryRoute

@Serializable
data object ScanMusicRoute

@Serializable
data object SearchRoute

@Serializable
data class AddSongsRoute(val playlistId: Long)

/** Reordering and removing a real playlist's songs; Favourites has no order to edit. */
@Serializable
data class EditPlaylistRoute(val playlistId: Long)

/** The Playlist Page. A null [playlistId] is the virtual Favourites, which has no row id. */
@Serializable
data class PlaylistDetailRoute(val playlistId: Long? = null)

/**
 * Library → Scan music, Search, Playlist Page, Add songs and Edit playlist all stay inside this
 * feature, so the graph navigates on its own. Opening the player (from the mini player or a search
 * result) crosses into another feature, so `:app` handles [onOpenPlayer]. [navigation] is the app's
 * Library/Downloader switch, shown on the Library screen only.
 */
fun NavGraphBuilder.libraryGraph(
    navController: NavController,
    onOpenPlayer: () -> Unit,
    navigation: @Composable () -> Unit,
) {
    navigation<LibraryGraph>(startDestination = LibraryRoute) {
        composable<LibraryRoute> {
            LibraryRoot(
                onScanClick = { navController.navigate(ScanMusicRoute) },
                onSearchClick = { navController.navigate(SearchRoute) },
                onPlaylistCreated = { playlistId -> navController.navigate(AddSongsRoute(playlistId)) },
                onPlaylistClick = { playlistId -> navController.navigate(PlaylistDetailRoute(playlistId)) },
                onMiniPlayerClick = onOpenPlayer,
                navigation = navigation,
            )
        }
        composable<ScanMusicRoute> {
            ScanMusicRoot(
                onNavigateBack = { navController.navigateUp() },
            )
        }
        composable<SearchRoute> {
            SearchRoot(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToPlayer = onOpenPlayer,
            )
        }
        composable<PlaylistDetailRoute> { backStackEntry ->
            PlaylistDetailRoot(
                playlistId = backStackEntry.toRoute<PlaylistDetailRoute>().playlistId,
                onNavigateBack = { navController.navigateUp() },
                onAddSongsClick = { playlistId -> navController.navigate(AddSongsRoute(playlistId)) },
                onEditPlaylistClick = { playlistId -> navController.navigate(EditPlaylistRoute(playlistId)) },
            )
        }
        composable<AddSongsRoute> { backStackEntry ->
            AddSongsRoot(
                playlistId = backStackEntry.toRoute<AddSongsRoute>().playlistId,
                onNavigateBack = { navController.navigateUp() },
            )
        }
        composable<EditPlaylistRoute> { backStackEntry ->
            EditPlaylistRoot(
                playlistId = backStackEntry.toRoute<EditPlaylistRoute>().playlistId,
                onNavigateBack = { navController.navigateUp() },
            )
        }
    }
}
