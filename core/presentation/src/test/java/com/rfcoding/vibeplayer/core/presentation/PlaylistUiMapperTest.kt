package com.rfcoding.vibeplayer.core.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.rfcoding.vibeplayer.core.domain.playlist.Playlist
import com.rfcoding.vibeplayer.core.domain.song.Song
import org.junit.jupiter.api.Test

class PlaylistUiMapperTest {

    private fun song(id: String, imageUri: String?) = Song(
        id = id,
        title = id,
        artistName = null,
        fileUri = "content://$id",
        imageUri = imageUri,
        durationMillis = 60_000,
        isFavorite = false,
        createdAt = 0,
    )

    private fun playlist(coverUri: String?, songs: List<Song>) =
        Playlist(id = 1, name = "Chill", createdAt = 0, songs = songs, coverUri = coverUri)

    @Test
    fun `a picked cover wins over the songs' artwork`() {
        val ui = playlist(coverUri = "content://cover", songs = listOf(song("a", "content://art"))).toPlaylistUi()

        assertThat(ui.imageUri).isEqualTo("content://cover")
    }

    @Test
    fun `without a cover the first song that has artwork lends it`() {
        val ui = playlist(
            coverUri = null,
            songs = listOf(song("a", null), song("b", "content://b-art"), song("c", "content://c-art")),
        ).toPlaylistUi()

        assertThat(ui.imageUri).isEqualTo("content://b-art")
    }

    @Test
    fun `without a cover or any artwork there is no image`() {
        assertThat(playlist(coverUri = null, songs = listOf(song("a", null))).toPlaylistUi().imageUri).isNull()
    }
}
