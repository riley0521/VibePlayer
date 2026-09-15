package com.rfcoding.vibeplayer.feature.player.presentation

import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.presentation.SongUi

data class PlayerState(
    /** Null while nothing is queued, e.g. before the session connects. */
    val song: SongUi? = null,
    val isPlaying: Boolean = false,
    val positionMillis: Long = 0,
    val isFavorite: Boolean = false,
    val isShuffleOn: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    /** At most one sheet is open at a time, so one slot beats a boolean per sheet. */
    val activeSheet: PlayerSheet? = null,
)

/**
 * The bottom sheets the Player screen can put over itself. Each keeps the id of the song that was
 * playing when it opened, so that song is the one added even if the track changes meanwhile.
 */
sealed interface PlayerSheet {
    val songId: String

    /** Figma "Now Playing - Add to Playlist". It keeps its own state in `AddToPlaylistViewModel`. */
    data class AddToPlaylist(override val songId: String) : PlayerSheet

    /** The shared create-playlist sheet; the new playlist gets [songId] once it is saved. */
    data class CreatePlaylist(override val songId: String) : PlayerSheet
}
