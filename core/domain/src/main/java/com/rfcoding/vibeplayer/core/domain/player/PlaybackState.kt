package com.rfcoding.vibeplayer.core.domain.player

import com.rfcoding.vibeplayer.core.domain.song.Song

/**
 * A mirror of the playback session. Every field comes from the session in the playback service, so it
 * stays correct while only the service is running.
 */
data class PlaybackState(
    /** The whole queue, in play order: songs already played, the current one, then the upcoming ones. */
    val queue: List<Song> = emptyList(),
    /** Index of the current song in [queue], or -1 when nothing is queued. */
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMillis: Long = 0,
    val isShuffleOn: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    /** Song ids in the order the queue was started, which turning shuffle off restores. */
    val originalOrder: List<String> = emptyList(),
) {
    val currentSong: Song?
        get() = queue.getOrNull(currentIndex)

    /** With [RepeatMode.All] the queue wraps around, so the first song has a previous one too. */
    val hasPrevious: Boolean
        get() = currentIndex > 0 || isLooping

    val hasNext: Boolean
        get() = currentIndex in 0 until queue.lastIndex || isLooping

    /**
     * The song a swipe of the artwork goes back to. Unlike [hasPrevious], swipes always wrap around, so
     * the first song's previous is the last whatever the repeat mode. Null with fewer than two songs.
     */
    val swipePreviousIndex: Int?
        get() = swipeIndex(-1)

    /** The song a swipe of the artwork goes on to; wraps from the last song to the first, see [swipePreviousIndex]. */
    val swipeNextIndex: Int?
        get() = swipeIndex(1)

    val swipePreviousSong: Song?
        get() = swipePreviousIndex?.let(queue::get)

    val swipeNextSong: Song?
        get() = swipeNextIndex?.let(queue::get)

    private fun swipeIndex(step: Int): Int? =
        if (queue.size < 2 || currentIndex !in queue.indices) null else (currentIndex + step).mod(queue.size)

    private val isLooping: Boolean
        get() = repeatMode == RepeatMode.All && queue.isNotEmpty()

    /**
     * The position a seek bar released at [fraction] points to, or null when nothing is loaded. It uses
     * the song's stored duration because that is what the seek bars draw their progress from.
     */
    fun seekPositionFor(fraction: Float): Long? {
        val song = currentSong ?: return null
        return (fraction.coerceIn(0f, 1f) * song.durationMillis).toLong()
    }
}
