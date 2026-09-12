package com.rfcoding.vibeplayer.feature.library.presentation.addsongs

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.rfcoding.vibeplayer.core.presentation.SongUi
import org.junit.jupiter.api.Test

class AddSongsStateTest {

    private val songs = listOf(
        song("les-passants.mp3"),
        song("505.mp3"),
        song("last-nite.mp3"),
    )

    @Test
    fun `select all is unchecked when the search matches nothing`() {
        val state = AddSongsState(
            query = "knw",
            songs = emptyList(),
            selectedSongIds = setOf("505.mp3"),
        )

        assertThat(state.isAllSelected).isFalse()
    }

    @Test
    fun `select all is unchecked while any visible song is unselected`() {
        val state = AddSongsState(songs = songs, selectedSongIds = setOf("505.mp3"))

        assertThat(state.isAllSelected).isFalse()
    }

    @Test
    fun `select all is checked once every visible song is selected`() {
        val state = AddSongsState(songs = songs, selectedSongIds = songs.map { it.id }.toSet())

        assertThat(state.isAllSelected).isTrue()
    }

    @Test
    fun `select all ignores songs the query filtered out`() {
        val state = AddSongsState(
            query = "Les",
            songs = songs.take(1),
            selectedSongIds = setOf("les-passants.mp3", "mr-brightside.mp3"),
        )

        assertThat(state.isAllSelected).isTrue()
    }

    @Test
    fun `there is no selection by default`() {
        val state = AddSongsState(songs = songs)

        assertThat(state.hasSelection).isFalse()
        assertThat(state.selectedCount).isEqualTo(0)
    }

    @Test
    fun `selected count follows the selected ids`() {
        val state = AddSongsState(songs = songs, selectedSongIds = setOf("505.mp3", "last-nite.mp3"))

        assertThat(state.hasSelection).isTrue()
        assertThat(state.selectedCount).isEqualTo(2)
    }

    private fun song(id: String) = SongUi(
        id = id,
        title = id.removeSuffix(".mp3"),
        artistName = null,
        imageUri = null,
        durationMillis = 200_000,
    )
}
