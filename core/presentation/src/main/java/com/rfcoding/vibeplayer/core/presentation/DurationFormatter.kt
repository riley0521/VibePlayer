package com.rfcoding.vibeplayer.core.presentation

import java.util.Locale

/**
 * Formats a duration for the song cards, the mini player and the player screen: "3:45", and
 * "1:02:03" once it passes an hour. Anything negative is treated as zero.
 */
fun Long.toDurationText(): String {
    val totalSeconds = (this / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        "%d:%02d:%02d".format(Locale.getDefault(), hours, minutes, seconds)
    } else {
        "%d:%02d".format(Locale.getDefault(), minutes, seconds)
    }
}
