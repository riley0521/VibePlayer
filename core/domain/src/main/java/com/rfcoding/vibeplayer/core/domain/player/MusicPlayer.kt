package com.rfcoding.vibeplayer.core.domain.player

import com.rfcoding.vibeplayer.core.domain.song.Song

/**
 * Plays songs for every feature. Callers build the queue themselves (including any shuffling), so the
 * player only ever plays a list from its first song.
 */
interface MusicPlayer {

    /** Replaces the current queue with [queue] and starts playing its first song. */
    suspend fun play(queue: List<Song>)
}
