package com.rfcoding.vibeplayer.core.presentation

import com.rfcoding.vibeplayer.core.domain.player.PlaybackState

/** What the [MiniPlayer] shows. */
data class NowPlayingUi(
    val song: SongUi,
    val isPlaying: Boolean = false,
    val positionMillis: Long = 0,
    /** False on the first song of the list, which hides the mini player's previous button. */
    val canSkipToPrevious: Boolean = false,
)

/** Null while nothing is loaded, which hides the mini player. */
fun PlaybackState.toNowPlayingUi(): NowPlayingUi? = currentSong?.let { song ->
    NowPlayingUi(
        song = song.toSongUi(),
        isPlaying = isPlaying,
        positionMillis = positionMillis,
        canSkipToPrevious = hasPrevious,
    )
}
