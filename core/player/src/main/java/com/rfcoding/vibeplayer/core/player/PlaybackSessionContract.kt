package com.rfcoding.vibeplayer.core.player

import android.os.Bundle
import androidx.media3.session.SessionCommand
import com.rfcoding.vibeplayer.core.domain.player.SleepTimer
import kotlin.time.Duration.Companion.milliseconds

/**
 * What [MusicManager], the media notification and [PlaybackService] agree on. State the player itself
 * can't hold (the shuffle flag, the original order and whether the queue is a playlist) is kept in the session extras, so it lives as
 * long as the service.
 */
internal object PlaybackSessionContract {

    /** Replaces the session extras with the queue info carried in the command's args. */
    val SetQueueInfoCommand = SessionCommand("com.rfcoding.vibeplayer.SET_QUEUE_INFO", Bundle.EMPTY)

    /** Shuffles the upcoming songs, or restores the original order when shuffle is on. */
    val ToggleShuffleCommand = SessionCommand("com.rfcoding.vibeplayer.TOGGLE_SHUFFLE", Bundle.EMPTY)

    /** Flips the current song's favourite flag in the database. */
    val ToggleFavoriteCommand = SessionCommand("com.rfcoding.vibeplayer.TOGGLE_FAVORITE", Bundle.EMPTY)

    /** Moves the upcoming song at [KEY_FROM] to [KEY_TO]; see [moveArgs]. */
    val MoveQueueItemCommand = SessionCommand("com.rfcoding.vibeplayer.MOVE_QUEUE_ITEM", Bundle.EMPTY)

    /** Removes the upcoming song at [KEY_INDEX]; see [removeArgs]. */
    val RemoveQueueItemCommand = SessionCommand("com.rfcoding.vibeplayer.REMOVE_QUEUE_ITEM", Bundle.EMPTY)

    /** Replaces the sleep timer with the one carried in the args; see [sleepTimerArgs]. */
    val SetSleepTimerCommand = SessionCommand("com.rfcoding.vibeplayer.SET_SLEEP_TIMER", Bundle.EMPTY)

    private const val KEY_IS_SHUFFLE_ON = "is_shuffle_on"
    private const val KEY_ORIGINAL_ORDER = "original_order"
    private const val KEY_IS_PLAYLIST = "is_playlist"
    private const val KEY_FROM = "from"
    private const val KEY_TO = "to"
    private const val KEY_INDEX = "index"
    private const val KEY_SLEEP_TIMER_MILLIS = "sleep_timer_millis"

    /** Stands for [SleepTimer.EndOfTrack] in [KEY_SLEEP_TIMER_MILLIS]. */
    private const val END_OF_TRACK = -1L

    fun queueInfoExtras(isShuffleOn: Boolean, originalOrder: List<String>, isPlaylist: Boolean): Bundle =
        Bundle().apply {
            putBoolean(KEY_IS_SHUFFLE_ON, isShuffleOn)
            putStringArrayList(KEY_ORIGINAL_ORDER, ArrayList(originalOrder))
            putBoolean(KEY_IS_PLAYLIST, isPlaylist)
        }

    fun Bundle.isShuffleOn(): Boolean = getBoolean(KEY_IS_SHUFFLE_ON, false)

    fun Bundle.originalOrder(): List<String> = getStringArrayList(KEY_ORIGINAL_ORDER).orEmpty()

    fun Bundle.isPlaylist(): Boolean = getBoolean(KEY_IS_PLAYLIST, false)

    fun moveArgs(from: Int, to: Int): Bundle = Bundle().apply {
        putInt(KEY_FROM, from)
        putInt(KEY_TO, to)
    }

    fun Bundle.moveFrom(): Int = getInt(KEY_FROM, -1)

    fun Bundle.moveTo(): Int = getInt(KEY_TO, -1)

    fun removeArgs(index: Int): Bundle = Bundle().apply { putInt(KEY_INDEX, index) }

    fun Bundle.removeIndex(): Int = getInt(KEY_INDEX, -1)

    fun sleepTimerArgs(timer: SleepTimer): Bundle = Bundle().apply {
        val millis = when (timer) {
            is SleepTimer.After -> timer.duration.inWholeMilliseconds
            SleepTimer.EndOfTrack -> END_OF_TRACK
        }
        putLong(KEY_SLEEP_TIMER_MILLIS, millis)
    }

    /** Null when the args carry no timer. */
    fun Bundle.sleepTimer(): SleepTimer? {
        val millis = getLong(KEY_SLEEP_TIMER_MILLIS, 0)
        return when {
            millis == END_OF_TRACK -> SleepTimer.EndOfTrack
            millis > 0 -> SleepTimer.After(millis.milliseconds)
            else -> null
        }
    }
}
