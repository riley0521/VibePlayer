package com.rfcoding.vibeplayer.feature.library.presentation.library

import androidx.compose.runtime.Stable
import com.rfcoding.vibeplayer.core.presentation.PlaylistUi
import com.rfcoding.vibeplayer.core.presentation.SongUi
import com.rfcoding.vibeplayer.feature.library.presentation.playlistname.PlaylistNameState

@Stable
data class LibraryState(
    val status: LibraryStatus = LibraryStatus.Scanning,
    val songs: List<SongUi> = emptyList(),
    val selectedTab: LibraryTab = LibraryTab.Songs,
    val nowPlaying: NowPlayingUi? = null,
    /** Drives the virtual Favourites card, which is always listed first and can't be deleted. */
    val favouriteSongCount: Int = 0,
    val playlists: List<PlaylistUi> = emptyList(),
    /** At most one sheet is open at a time, so one slot beats a boolean per sheet. */
    val activeSheet: LibrarySheet? = null,
)

/** The bottom sheets the main screen can put over itself. */
sealed interface LibrarySheet {

    /**
     * Figma "Main Page - Playlist + Action Sheet". A null [playlist] is the virtual Favourites, which
     * has no row id and so offers Play only.
     */
    data class PlaylistActions(
        val playlist: PlaylistUi?,
        val favouriteSongCount: Int = 0,
    ) : LibrarySheet {
        val songCount: Int get() = playlist?.songCount ?: favouriteSongCount
        val playAction: LibraryAction
            get() = playlist
                ?.let { LibraryAction.OnPlayPlaylistClick(it.id) }
                ?: LibraryAction.OnPlayFavouritesClick
    }

    /** Favourites can't be deleted, so this always names a real playlist. */
    data class DeletePlaylist(val playlist: PlaylistUi) : LibrarySheet

    /** Creating (a null [playlistId]) and renaming share one sheet. */
    data class PlaylistName(
        val state: PlaylistNameState,
        val playlistId: Long? = null,
    ) : LibrarySheet
}

enum class LibraryStatus {
    Scanning,
    NoMusicFound,
    Loaded,
}

enum class LibraryTab {
    Songs,
    Playlist,
}

data class NowPlayingUi(
    val song: SongUi,
    val isPlaying: Boolean = false,
    val positionMillis: Long = 0,
    /** False on the first song of the list, which hides the mini player's previous button. */
    val canSkipToPrevious: Boolean = false,
)
