package com.rfcoding.vibeplayer.core.domain.player

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import com.rfcoding.vibeplayer.core.domain.song.Song
import org.junit.jupiter.api.Test
import kotlin.random.Random

class QueueOrderTest {

    private val songs = (1..8).map { song("$it") }

    @Test
    fun `shuffling keeps played songs and the current song in place`() {
        val playback = PlaybackState(queue = songs, currentIndex = 2)

        val shuffled = playback.shuffledQueue(Random(42))

        assertThat(shuffled.take(3)).isEqualTo(songs.take(3))
    }

    @Test
    fun `shuffling reorders only the upcoming songs`() {
        val playback = PlaybackState(queue = songs, currentIndex = 2)

        val upcoming = playback.shuffledQueue(Random(42)).drop(3)

        assertThat(upcoming).containsExactlyInAnyOrder(*songs.drop(3).toTypedArray())
        assertThat(upcoming).isNotEqualTo(songs.drop(3))
    }

    @Test
    fun `shuffling the last song leaves the queue unchanged`() {
        val playback = PlaybackState(queue = songs, currentIndex = songs.lastIndex)

        assertThat(playback.shuffledQueue(Random(42))).isEqualTo(songs)
    }

    @Test
    fun `the original order puts every song back, the current one included`() {
        val (a, b, c, d) = songs
        val playback = PlaybackState(
            queue = listOf(a, c, d, b),
            currentIndex = 1,
            originalOrder = listOf(a, b, c, d).map { it.id },
        )

        assertThat(playback.originalOrderQueue()).containsExactly(a, b, c, d)
    }

    @Test
    fun `songs missing from the original order go last in their current order`() {
        val (a, b, c, d) = songs
        val playback = PlaybackState(
            queue = listOf(d, a, c, b),
            currentIndex = 0,
            originalOrder = listOf(b, a).map { it.id },
        )

        assertThat(playback.originalOrderQueue()).containsExactly(b, a, d, c)
    }

    @Test
    fun `with shuffle off a move becomes the original order`() {
        val (a, b, c, d) = songs
        val playback = PlaybackState(
            queue = listOf(a, b, c, d),
            currentIndex = 0,
            originalOrder = listOf(a, b, c, d).map { it.id },
        )

        assertThat(playback.originalOrderAfterMove(from = 3, to = 1)).containsExactly("1", "4", "2", "3")
    }

    @Test
    fun `with shuffle on a move leaves the original order alone`() {
        val (a, b, c, d) = songs
        val originalOrder = listOf(a, b, c, d).map { it.id }
        val playback = PlaybackState(
            queue = listOf(a, d, b, c),
            currentIndex = 0,
            isShuffleOn = true,
            originalOrder = originalOrder,
        )

        assertThat(playback.originalOrderAfterMove(from = 3, to = 1)).isEqualTo(originalOrder)
    }

    @Test
    fun `a move outside the queue leaves the original order alone`() {
        val playback = PlaybackState(queue = songs.take(2), currentIndex = 0, originalOrder = listOf("1", "2"))

        assertThat(playback.originalOrderAfterMove(from = 1, to = 5)).containsExactly("1", "2")
    }

    @Test
    fun `a removed song leaves the original order`() {
        val (a, b, c) = songs
        val playback = PlaybackState(
            queue = listOf(a, c, b),
            currentIndex = 0,
            isShuffleOn = true,
            originalOrder = listOf(a, b, c).map { it.id },
        )

        assertThat(playback.originalOrderAfterRemove(index = 1)).containsExactly("1", "2")
    }

    @Test
    fun `removing an index outside the queue leaves the original order alone`() {
        val playback = PlaybackState(queue = songs.take(2), currentIndex = 0, originalOrder = listOf("1", "2"))

        assertThat(playback.originalOrderAfterRemove(index = 4)).containsExactly("1", "2")
    }

    private fun song(id: String) = Song(
        id = id,
        title = id,
        artistName = null,
        fileUri = "content://$id",
        imageUri = null,
        durationMillis = 60_000,
        isFavorite = false,
        createdAt = 0,
    )
}
