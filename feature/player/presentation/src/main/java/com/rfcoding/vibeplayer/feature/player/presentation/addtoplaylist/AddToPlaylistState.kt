package com.rfcoding.vibeplayer.feature.player.presentation.addtoplaylist

import com.rfcoding.vibeplayer.core.presentation.PlaylistUi

data class AddToPlaylistState(
    /** Drives the virtual Favourites row, which is listed first after Create Playlist. */
    val favouriteSongCount: Int = 0,
    val playlists: List<PlaylistUi> = emptyList(),
)
