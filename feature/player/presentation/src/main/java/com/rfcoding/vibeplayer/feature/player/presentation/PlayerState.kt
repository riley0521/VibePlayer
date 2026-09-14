package com.rfcoding.vibeplayer.feature.player.presentation

import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.presentation.SongUi

data class PlayerState(
    /** Null while nothing is queued, e.g. before the session connects. */
    val song: SongUi? = null,
    val isPlaying: Boolean = false,
    val positionMillis: Long = 0,
    val isFavorite: Boolean = false,
    val isShuffleOn: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
)
