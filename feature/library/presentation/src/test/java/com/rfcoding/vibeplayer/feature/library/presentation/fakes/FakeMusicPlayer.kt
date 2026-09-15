package com.rfcoding.vibeplayer.feature.library.presentation.fakes

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

    override suspend fun seekTo(positionMillis: Long) {
        seekPositions += positionMillis
    }

    override suspend fun setRepeatMode(repeatMode: RepeatMode) = Unit

    override suspend fun toggleShuffle() = Unit
}
