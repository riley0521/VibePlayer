package com.rfcoding.vibeplayer.feature.player.presentation.queue

import com.rfcoding.vibeplayer.core.domain.player.SleepTimer
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/** The rows of the Sleep timer sheet, in the order it lists them. */
enum class SleepTimerOption(val timer: SleepTimer) {
    Minutes5(SleepTimer.After(5.minutes)),
    Minutes10(SleepTimer.After(10.minutes)),
    Minutes15(SleepTimer.After(15.minutes)),
    Minutes30(SleepTimer.After(30.minutes)),
    Minutes45(SleepTimer.After(45.minutes)),
    Hour1(SleepTimer.After(1.hours)),
    EndOfTrack(SleepTimer.EndOfTrack),
}
