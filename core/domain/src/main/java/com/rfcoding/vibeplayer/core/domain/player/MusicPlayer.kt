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
     * and [queue]'s order becomes the original order. [isPlaylist] marks a queue started from a playlist,
     * whose artwork swipes wrap around whatever the repeat mode; see [PlaybackState.swipeNextIndex].
     */
    suspend fun play(queue: List<Song>, isPlaylist: Boolean = false)

    /** Pauses while playing; otherwise plays, restarting the current song if the queue has ended. */
    suspend fun togglePlayPause()

    suspend fun skipToNext()

    /** Goes to the previous song, or restarts the current one when it is the first. */
    suspend fun skipToPrevious()

    /** Jumps to the start of the song at [index] in [PlaybackState.queue], keeping play/pause as it is. */
    suspend fun skipTo(index: Int)

    /** Moves the current song to [positionMillis]; see [PlaybackState.seekPositionFor]. */
    suspend fun seekTo(positionMillis: Long)

    suspend fun setRepeatMode(repeatMode: RepeatMode)

    /**
     * Shuffles the upcoming songs, or restores the original order when shuffle is on, without
     * interrupting the current song. See [shuffledQueue] and [originalOrderQueue].
     */
    suspend fun toggleShuffle()

    /**
     * Moves the upcoming song at [from] to [to], both indices in [PlaybackState.queue]. Neither may be
     * the current song or one already played; such a move is ignored. See [originalOrderAfterMove].
     */
    suspend fun moveQueueItem(from: Int, to: Int)

    /** Removes the upcoming song at [index] in [PlaybackState.queue]; the current song can't be removed. */
    suspend fun removeQueueItem(index: Int)

    /** Pauses and stops the playback service when [timer] is due, replacing any timer already set. */
    suspend fun setSleepTimer(timer: SleepTimer)
}
