package com.rfcoding.vibeplayer.core.testing

import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.player.PlaybackState
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.domain.song.Song
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMusicPlayer : MusicPlayer {
    override val playbackState = MutableStateFlow(PlaybackState())

    val playedQueues = mutableListOf<List<Song>>()
    var togglePlayPauseCount = 0
    var skipToNextCount = 0
    var skipToPreviousCount = 0
    val skippedToIndices = mutableListOf<Int>()
    var toggleShuffleCount = 0
    val repeatModes = mutableListOf<RepeatMode>()
    val seekPositions = mutableListOf<Long>()

    override suspend fun play(queue: List<Song>) {
        playedQueues += queue
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
}
