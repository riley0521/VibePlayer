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
