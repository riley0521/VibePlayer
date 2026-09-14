package com.rfcoding.vibeplayer.core.player

import android.os.Bundle
import androidx.media3.session.SessionCommand

/**
 * What [MusicManager], the media notification and [PlaybackService] agree on. State the player itself
 * can't hold (the shuffle flag and the original order) is kept in the session extras, so it lives as
 * long as the service.
 */
internal object PlaybackSessionContract {

    /** Replaces the session extras with the queue info carried in the command's args. */
    val SetQueueInfoCommand = SessionCommand("com.rfcoding.vibeplayer.SET_QUEUE_INFO", Bundle.EMPTY)

    /** Shuffles the upcoming songs, or restores the original order when shuffle is on. */
    val ToggleShuffleCommand = SessionCommand("com.rfcoding.vibeplayer.TOGGLE_SHUFFLE", Bundle.EMPTY)

    /** Flips the current song's favourite flag in the database. */
    val ToggleFavoriteCommand = SessionCommand("com.rfcoding.vibeplayer.TOGGLE_FAVORITE", Bundle.EMPTY)

    private const val KEY_IS_SHUFFLE_ON = "is_shuffle_on"
    private const val KEY_ORIGINAL_ORDER = "original_order"

    fun queueInfoExtras(isShuffleOn: Boolean, originalOrder: List<String>): Bundle = Bundle().apply {
        putBoolean(KEY_IS_SHUFFLE_ON, isShuffleOn)
        putStringArrayList(KEY_ORIGINAL_ORDER, ArrayList(originalOrder))
    }

    fun Bundle.isShuffleOn(): Boolean = getBoolean(KEY_IS_SHUFFLE_ON, false)

    fun Bundle.originalOrder(): List<String> = getStringArrayList(KEY_ORIGINAL_ORDER).orEmpty()
}
