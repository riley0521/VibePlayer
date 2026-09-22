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

/**
 * The original order once the song at [from] has moved to [to]. With shuffle off the move becomes
 * part of the original order, so toggling shuffle keeps it; with shuffle on only the shuffled order
 * changes, and turning shuffle off restores the order the queue was started in.
 */
fun PlaybackState.originalOrderAfterMove(from: Int, to: Int): List<String> {
    if (isShuffleOn || from !in queue.indices || to !in queue.indices) return originalOrder
    return queue.map { it.id }.toMutableList().apply { add(to, removeAt(from)) }
}

/** The original order without the song at [index], which is leaving the queue. */
fun PlaybackState.originalOrderAfterRemove(index: Int): List<String> {
    val removedId = queue.getOrNull(index)?.id ?: return originalOrder
    return originalOrder - removedId
}
