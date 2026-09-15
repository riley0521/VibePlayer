package com.rfcoding.vibeplayer.core.domain.player

import com.rfcoding.vibeplayer.core.domain.song.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * Plays songs for every feature. Callers build the queue they start (including any shuffling), so the
 * player only ever plays a list from its first song. The shuffle toggle is the exception: the playback
 * service performs it, because the media notification offers it too.
 */
interface MusicPlayer {

    val playbackState: StateFlow<PlaybackState>

    /**
     * Replaces the current queue with [queue] and starts playing its first song. Shuffle is turned off
     * and [queue]'s order becomes the original order.
     */
    suspend fun play(queue: List<Song>)

    /** Pauses while playing; otherwise plays, restarting the current song if the queue has ended. */
    suspend fun togglePlayPause()

    suspend fun skipToNext()

    /** Goes to the previous song, or restarts the current one when it is the first. */
    suspend fun skipToPrevious()

    /** Moves the current song to [positionMillis]; see [PlaybackState.seekPositionFor]. */
    suspend fun seekTo(positionMillis: Long)

    suspend fun setRepeatMode(repeatMode: RepeatMode)

    /**
     * Shuffles the upcoming songs, or restores the original order when shuffle is on, without
     * interrupting the current song. See [shuffledQueue] and [originalOrderQueue].
     */
    suspend fun toggleShuffle()
}
