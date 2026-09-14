package com.rfcoding.vibeplayer.core.domain.player

import com.rfcoding.vibeplayer.core.domain.song.Song
import kotlin.random.Random

/**
 * The queue with only the upcoming songs shuffled: songs already played and the current one keep
 * their place.
 */
fun PlaybackState.shuffledQueue(random: Random = Random): List<Song> =
    queue.take(currentIndex + 1) + queue.drop(currentIndex + 1).shuffled(random)

/**
 * The queue back in the order it was started, so the current song returns to its original place.
 * Songs missing from [PlaybackState.originalOrder] go last, in their current order.
 */
fun PlaybackState.originalOrderQueue(): List<Song> {
    val originalPosition = originalOrder.withIndex().associate { (index, id) -> id to index }
    return queue.sortedBy { originalPosition[it.id] ?: Int.MAX_VALUE }
}
