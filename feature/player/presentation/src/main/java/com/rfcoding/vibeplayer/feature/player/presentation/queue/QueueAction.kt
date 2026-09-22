package com.rfcoding.vibeplayer.feature.player.presentation.queue

sealed interface QueueAction {
    data object OnPlayPauseClick : QueueAction
    data class OnUpcomingSongClick(val songId: String) : QueueAction
    /** One step of a drag; [from] and [to] are indices in [QueueState.upcomingSongs]. */
    data class OnMoveSong(val from: Int, val to: Int) : QueueAction
    /** The drag was released; only now does the player get the move. */
    data object OnDragStopped : QueueAction
    data class OnSongSwiped(val songId: String) : QueueAction
    data object OnShuffleClick : QueueAction
    data object OnRepeatClick : QueueAction
    data object OnTimerClick : QueueAction
    data class OnSleepTimerSelect(val option: SleepTimerOption) : QueueAction
    data object OnSleepTimerSheetDismiss : QueueAction
}
