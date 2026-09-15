package com.rfcoding.vibeplayer.core.domain.player

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.rfcoding.vibeplayer.core.domain.song.Song
import org.junit.jupiter.api.Test

class PlaybackStateTest {

    private val songs = listOf("a", "b", "c").map { id ->
        Song(
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

    private fun playback(currentIndex: Int, repeatMode: RepeatMode = RepeatMode.Off) =
        PlaybackState(queue = songs, currentIndex = currentIndex, repeatMode = repeatMode)

    @Test
    fun `without repeat the first song has no previous and the last has no next`() {
        assertThat(playback(0).hasPrevious).isFalse()
        assertThat(playback(0).hasNext).isTrue()
        assertThat(playback(2).hasPrevious).isTrue()
        assertThat(playback(2).hasNext).isFalse()
    }

    @Test
    fun `a middle song has both a previous and a next song`() {
        assertThat(playback(1).hasPrevious).isTrue()
        assertThat(playback(1).hasNext).isTrue()
    }

    @Test
    fun `repeat all wraps around at both ends`() {
        assertThat(playback(0, RepeatMode.All).hasPrevious).isTrue()
        assertThat(playback(2, RepeatMode.All).hasNext).isTrue()
    }

    @Test
    fun `repeat one navigates like repeat off`() {
        assertThat(playback(0, RepeatMode.One).hasPrevious).isFalse()
        assertThat(playback(2, RepeatMode.One).hasNext).isFalse()
    }

    @Test
    fun `an empty queue has neither a previous nor a next song`() {
        val empty = PlaybackState(repeatMode = RepeatMode.All)

        assertThat(empty.hasPrevious).isFalse()
        assertThat(empty.hasNext).isFalse()
    }

    @Test
    fun `the seek position is the fraction of the current song's duration`() {
        assertThat(playback(1).seekPositionFor(0f)).isEqualTo(0L)
        assertThat(playback(1).seekPositionFor(0.5f)).isEqualTo(30_000L)
        assertThat(playback(1).seekPositionFor(1f)).isEqualTo(60_000L)
    }

    @Test
    fun `a seek fraction outside the bar is clamped to the song`() {
        assertThat(playback(1).seekPositionFor(-0.2f)).isEqualTo(0L)
        assertThat(playback(1).seekPositionFor(1.5f)).isEqualTo(60_000L)
    }

    @Test
    fun `nothing to seek when no song is loaded`() {
        assertThat(PlaybackState().seekPositionFor(0.5f)).isNull()
    }
}
