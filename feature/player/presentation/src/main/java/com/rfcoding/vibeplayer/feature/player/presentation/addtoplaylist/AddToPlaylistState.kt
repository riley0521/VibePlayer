package com.rfcoding.vibeplayer.feature.player.presentation.addtoplaylist

import com.rfcoding.vibeplayer.core.presentation.PlaylistUi

data class AddToPlaylistState(
    /** Drives the virtual Favourites row, which is listed first after Create Playlist. */
    val favouriteSongCount: Int = 0,
    /** Whether the song is a favourite, which gives the Favourites row its check icon. */
    val isFavourite: Boolean = false,
    val playlists: List<PlaylistUi> = emptyList(),
    /** The playlists that already hold the song; their rows show a check icon instead of a plus. */
    val playlistIdsWithSong: Set<Long> = emptySet(),
)
