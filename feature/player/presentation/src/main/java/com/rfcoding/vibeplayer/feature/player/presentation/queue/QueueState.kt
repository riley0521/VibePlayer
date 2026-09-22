package com.rfcoding.vibeplayer.feature.player.presentation.queue

import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.presentation.SongUi

data class QueueState(
    /** Null while nothing is queued. */
    val currentSong: SongUi? = null,
    val isPlaying: Boolean = false,
    /** The songs after the current one, in play order; songs already played aren't listed. */
    val upcomingSongs: List<SongUi> = emptyList(),
    val isShuffleOn: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    /** The Sleep timer sheet opens over the Queue sheet, which stays open beneath it. */
    val isSleepTimerSheetVisible: Boolean = false,
)
