package com.rfcoding.vibeplayer.feature.library.domain

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.rfcoding.vibeplayer.core.domain.song.Song
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SongSearchTest {

    private val songs = listOf(
        song(id = "1", title = "Les passants", artistName = "Zaz"),
        song(id = "2", title = "The Less I Know The Better", artistName = "Tame Impala"),
        song(id = "3", title = "505", artistName = "Arctic Monkeys"),
        song(id = "4", title = "House of the Rising Sun", artistName = null),
    )

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "\t  "])
    fun `a blank query keeps every song`(query: String) {
        assertThat(songs.filterByQuery(query)).isEqualTo(songs)
    }

    @Test
    fun `matches part of the title`() {
        assertThat(songs.filterByQuery("les").map { it.id }).containsExactly("1", "2")
    }

    @Test
    fun `matches part of the artist name`() {
        assertThat(songs.filterByQuery("monkeys").map { it.id }).containsExactly("3")
    }

    @Test
    fun `ignores case and surrounding whitespace`() {
        assertThat(songs.filterByQuery("  TAME impala ").map { it.id }).containsExactly("2")
    }

    @Test
    fun `a song without an artist can still match by title`() {
        assertThat(songs.filterByQuery("rising").map { it.id }).containsExactly("4")
    }

    @Test
    fun `no match returns an empty list`() {
        assertThat(songs.filterByQuery("knw")).isEmpty()
    }

    private fun song(id: String, title: String, artistName: String?) = Song(
        id = id,
        title = title,
        artistName = artistName,
        fileUri = "content://$id",
        imageUri = null,
        durationMillis = 200_000,
        isFavorite = false,
        createdAt = 0,
    )
}
