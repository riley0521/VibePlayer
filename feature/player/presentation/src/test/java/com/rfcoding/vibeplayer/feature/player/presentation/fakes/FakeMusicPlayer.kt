package com.rfcoding.vibeplayer.feature.player.presentation.fakes

import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.player.PlaybackState
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.domain.song.Song
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMusicPlayer : MusicPlayer {
    override val playbackState = MutableStateFlow(PlaybackState())

    var togglePlayPauseCount = 0
    var skipToNextCount = 0
    var skipToPreviousCount = 0
    var toggleShuffleCount = 0
    val repeatModes = mutableListOf<RepeatMode>()

    override suspend fun play(queue: List<Song>) = Unit

    override suspend fun togglePlayPause() {
        togglePlayPauseCount++
    }

    override suspend fun skipToNext() {
        skipToNextCount++
    }

    override suspend fun skipToPrevious() {
        skipToPreviousCount++
    }

    override suspend fun setRepeatMode(repeatMode: RepeatMode) {
        repeatModes += repeatMode
    }

    override suspend fun toggleShuffle() {
        toggleShuffleCount++
    }
}

fun song(id: String, isFavorite: Boolean = false) = Song(
    id = id,
    title = "Song $id",
    artistName = null,
    fileUri = "content://$id",
    imageUri = null,
    durationMillis = 60_000,
    isFavorite = isFavorite,
    createdAt = 0,
)
