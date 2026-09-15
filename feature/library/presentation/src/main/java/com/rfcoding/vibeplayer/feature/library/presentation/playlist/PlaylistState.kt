package com.rfcoding.vibeplayer.feature.library.presentation.playlist

import com.rfcoding.vibeplayer.core.presentation.PlaylistUi
import com.rfcoding.vibeplayer.core.presentation.playlistname.PlaylistNameMode

data class PlaylistState(
    /** Drives the virtual Favourites card, which is always listed first and can't be deleted. */
    val favouriteSongCount: Int = 0,
    val playlists: List<PlaylistUi> = emptyList(),
    /** At most one sheet is open at a time, so one slot beats a boolean per sheet. */
    val activeSheet: PlaylistSheet? = null,
)

/** The bottom sheets the main screen can put over itself. */
sealed interface PlaylistSheet {

    /**
     * Figma "Main Page - Playlist + Action Sheet". A null [playlist] is the virtual Favourites, which
     * has no row id and so offers Play only.
     */
    data class PlaylistActions(
        val playlist: PlaylistUi?,
        val favouriteSongCount: Int = 0,
    ) : PlaylistSheet {
        val songCount: Int get() = playlist?.songCount ?: favouriteSongCount
        val playAction: PlaylistAction
            get() = playlist
                ?.let { PlaylistAction.OnPlayPlaylistClick(it.id) }
                ?: PlaylistAction.OnPlayFavouritesClick
    }

    /** Favourites can't be deleted, so this always names a real playlist. */
    data class DeletePlaylist(val playlist: PlaylistUi) : PlaylistSheet

    /** Creating and renaming share one sheet; it keeps its own state in `PlaylistNameViewModel`. */
    data class PlaylistName(val mode: PlaylistNameMode) : PlaylistSheet
}
