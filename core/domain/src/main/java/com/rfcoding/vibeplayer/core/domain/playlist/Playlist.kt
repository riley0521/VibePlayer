package com.rfcoding.vibeplayer.core.domain.playlist

import com.rfcoding.vibeplayer.core.domain.song.Song

/** A user-created playlist. Favourites is virtual (built from `Song.isFavorite`) and never one of these. */
data class Playlist(
    val id: Long,
    val name: String,
    /** Epoch milliseconds. */
    val createdAt: Long,
    val songs: List<Song>,
)
