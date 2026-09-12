package com.rfcoding.vibeplayer.feature.player.presentation

import com.rfcoding.vibeplayer.core.presentation.SongUi

data class PlayerState(
    val song: SongUi,
    val isPlaying: Boolean = false,
    val positionMillis: Long = 0,
    val isFavorite: Boolean = false,
    val isShuffleOn: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
)

enum class RepeatMode {
    Off,
    All,
    One,
}
