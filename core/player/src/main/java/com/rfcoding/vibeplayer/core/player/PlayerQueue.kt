package com.rfcoding.vibeplayer.core.player

import android.os.Bundle
import androidx.media3.common.Player
import com.rfcoding.vibeplayer.core.domain.player.PlaybackState
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.player.PlaybackSessionContract.isShuffleOn
import com.rfcoding.vibeplayer.core.player.PlaybackSessionContract.originalOrder

/*
 * Shared by MusicManager, which reads a MediaController, and PlaybackService, which drives the
 * ExoPlayer itself, so both derive the same PlaybackState.
 */

internal fun Player.readQueue(): List<Song> = List(mediaItemCount) { getMediaItemAt(it).toSong() }

internal fun Player.toPlaybackState(queue: List<Song>, sessionExtras: Bundle): PlaybackState = PlaybackState(
    queue = queue,
    currentIndex = if (queue.isEmpty()) -1 else currentMediaItemIndex,
    // Unlike Player.isPlaying this stays true while buffering, so the icon doesn't flicker.
    isPlaying = playWhenReady &&
        playbackState != Player.STATE_IDLE &&
        playbackState != Player.STATE_ENDED &&
        playbackSuppressionReason == Player.PLAYBACK_SUPPRESSION_REASON_NONE,
    positionMillis = currentPosition,
    isShuffleOn = sessionExtras.isShuffleOn(),
    repeatMode = repeatMode.toRepeatMode(),
    originalOrder = sessionExtras.originalOrder(),
)

/** Gives the queue [queue]'s order without interrupting the current song, which [queue] must contain. */
internal fun Player.reorderQueue(queue: List<Song>) {
    val currentIndex = currentMediaItemIndex
    val currentId = currentMediaItem?.mediaId ?: return
    val newCurrentIndex = queue.indexOfFirst { it.id == currentId }
    if (newCurrentIndex == -1) return

    // Neither range includes the current item, so the song keeps playing uninterrupted.
    replaceMediaItems(currentIndex + 1, mediaItemCount, queue.drop(newCurrentIndex + 1).map { it.toMediaItem() })
    replaceMediaItems(0, currentIndex, queue.take(newCurrentIndex).map { it.toMediaItem() })
}

internal fun RepeatMode.toMedia3(): Int = when (this) {
    RepeatMode.Off -> Player.REPEAT_MODE_OFF
    RepeatMode.All -> Player.REPEAT_MODE_ALL
    RepeatMode.One -> Player.REPEAT_MODE_ONE
}

private fun Int.toRepeatMode(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ALL -> RepeatMode.All
    Player.REPEAT_MODE_ONE -> RepeatMode.One
    else -> RepeatMode.Off
}
