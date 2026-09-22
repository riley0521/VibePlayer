package com.rfcoding.vibeplayer.core.domain.player

import kotlin.time.Duration

/** When the playback service pauses and stops itself. */
sealed interface SleepTimer {
    data class After(val duration: Duration) : SleepTimer

    /** Stops once the song playing at that moment, or the one it gives way to, ends. */
    data object EndOfTrack : SleepTimer
}
