package com.rfcoding.vibeplayer.core.database.playlist

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.rfcoding.vibeplayer.core.database.entity.PlaylistSongCrossRef
import org.junit.jupiter.api.Test

class CrossRefDiffTest {

    @Test
    fun `songs added to an empty playlist are numbered from zero`() {
        val crossRefs = appendCrossRefs(
            existing = emptyList(),
            playlistId = PLAYLIST_ID,
            songIds = listOf("a", "b"),
            addedAt = 7,
        )

        assertThat(crossRefs).containsExactly(
            crossRef("a", position = 0, addedAt = 7),
            crossRef("b", position = 1, addedAt = 7),
        )
    }

    @Test
    fun `songs are appended after the playlist's last position, gaps and all`() {
        val existing = listOf(crossRef("a", position = 0), crossRef("b", position = 4))

        val crossRefs = appendCrossRefs(existing, PLAYLIST_ID, songIds = listOf("c"), addedAt = 7)

        assertThat(crossRefs).containsExactly(crossRef("c", position = 5, addedAt = 7))
    }

    @Test
    fun `songs already in the playlist, and ids repeated in one call, are skipped`() {
        val existing = listOf(crossRef("a", position = 0))

        val crossRefs = appendCrossRefs(existing, PLAYLIST_ID, listOf("a", "b", "b"), addedAt = 7)

        assertThat(crossRefs).containsExactly(crossRef("b", position = 1, addedAt = 7))
    }

    @Test
    fun `a new order renumbers the songs from zero and keeps the date they were added`() {
        val existing = listOf(
            crossRef("a", position = 0, addedAt = 1),
            crossRef("b", position = 1, addedAt = 2),
            crossRef("c", position = 2, addedAt = 3),
        )

        val diff = diffCrossRefOrder(existing, songIds = listOf("c", "a", "b"))

        assertThat(diff.removedSongIds).isEmpty()
        assertThat(diff.upserts).containsExactly(
            crossRef("c", position = 0, addedAt = 3),
            crossRef("a", position = 1, addedAt = 1),
            crossRef("b", position = 2, addedAt = 2),
        )
    }

    @Test
    fun `songs left out of the new order are removed and the rest are compacted`() {
        val existing = listOf(
            crossRef("a", position = 0),
            crossRef("b", position = 1),
            crossRef("c", position = 2),
        )

        val diff = diffCrossRefOrder(existing, songIds = listOf("c", "a"))

        assertThat(diff.removedSongIds).containsExactly("b")
        assertThat(diff.upserts).containsExactly(
            crossRef("c", position = 0),
            crossRef("a", position = 1),
        )
    }

    @Test
    fun `an id the playlist does not hold is ignored rather than inserted`() {
        val existing = listOf(crossRef("a", position = 0))

        val diff = diffCrossRefOrder(existing, songIds = listOf("ghost", "a"))

        assertThat(diff.removedSongIds).isEmpty()
        assertThat(diff.upserts).containsExactly(crossRef("a", position = 0))
    }

    @Test
    fun `an empty order removes every song`() {
        val existing = listOf(crossRef("a", position = 0), crossRef("b", position = 1))

        val diff = diffCrossRefOrder(existing, songIds = emptyList())

        assertThat(diff.removedSongIds).containsExactly("a", "b")
        assertThat(diff.upserts).isEmpty()
    }

    private fun crossRef(songId: String, position: Int, addedAt: Long = 0) = PlaylistSongCrossRef(
        playlistId = PLAYLIST_ID,
        songId = songId,
        addedAt = addedAt,
        position = position,
    )

    private companion object {
        const val PLAYLIST_ID = 1L
    }
}
