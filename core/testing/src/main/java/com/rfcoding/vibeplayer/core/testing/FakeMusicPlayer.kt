package com.rfcoding.vibeplayer.core.testing

import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.player.PlaybackState
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.domain.player.SleepTimer
import com.rfcoding.vibeplayer.core.domain.song.Song
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMusicPlayer : MusicPlayer {
    override val playbackState = MutableStateFlow(PlaybackState())

    val playedQueues = mutableListOf<List<Song>>()
    /** The `isPlaylist` flag of each [play] call, in step with [playedQueues]. */
    val playedAsPlaylist = mutableListOf<Boolean>()
    var togglePlayPauseCount = 0
    var skipToNextCount = 0
    var skipToPreviousCount = 0
    val skippedToIndices = mutableListOf<Int>()
    var toggleShuffleCount = 0
    val repeatModes = mutableListOf<RepeatMode>()
    val seekPositions = mutableListOf<Long>()
    /** The (from, to) of each [moveQueueItem] call. */
    val movedQueueItems = mutableListOf<Pair<Int, Int>>()
    val removedQueueIndices = mutableListOf<Int>()
    val sleepTimers = mutableListOf<SleepTimer>()

    override suspend fun play(queue: List<Song>, isPlaylist: Boolean) {
        playedQueues += queue
        playedAsPlaylist += isPlaylist
    }

    override suspend fun togglePlayPause() {
        togglePlayPauseCount++
    }

    override suspend fun skipToNext() {
        skipToNextCount++
    }

    override suspend fun skipToPrevious() {
        skipToPreviousCount++
    }

    override suspend fun skipTo(index: Int) {
        skippedToIndices += index
    }

    override suspend fun seekTo(positionMillis: Long) {
        seekPositions += positionMillis
    }

    override suspend fun setRepeatMode(repeatMode: RepeatMode) {
        repeatModes += repeatMode
    }

    override suspend fun toggleShuffle() {
        toggleShuffleCount++
    }

    override suspend fun moveQueueItem(from: Int, to: Int) {
        movedQueueItems += from to to
    }

    override suspend fun removeQueueItem(index: Int) {
        removedQueueIndices += index
    }

    override suspend fun setSleepTimer(timer: SleepTimer) {
        sleepTimers += timer
    }
}
