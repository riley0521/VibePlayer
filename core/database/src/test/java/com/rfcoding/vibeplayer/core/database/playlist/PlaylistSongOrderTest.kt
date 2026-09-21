package com.rfcoding.vibeplayer.core.database.playlist

import assertk.assertThat
import assertk.assertions.containsExactly
import com.rfcoding.vibeplayer.core.database.entity.PlaylistEntity
import com.rfcoding.vibeplayer.core.database.entity.PlaylistSongCrossRef
import com.rfcoding.vibeplayer.core.database.entity.PlaylistWithSongs
import com.rfcoding.vibeplayer.core.database.entity.SongEntity
import org.junit.jupiter.api.Test

class PlaylistSongOrderTest {

    @Test
    fun `songs come back in the order their cross-refs give, not the query's`() {
        val playlist = playlistWithSongs(
            songIds = listOf("a", "b", "c"),
            positions = mapOf("a" to 2, "b" to 0, "c" to 1),
        )

        assertThat(playlist.songsInPlaylistOrder().map { it.id }).containsExactly("b", "c", "a")
    }

    @Test
    fun `gaps left by earlier removals only affect the relative order`() {
        val playlist = playlistWithSongs(
            songIds = listOf("a", "b"),
            positions = mapOf("a" to 9, "b" to 3),
        )

        assertThat(playlist.songsInPlaylistOrder().map { it.id }).containsExactly("b", "a")
    }

    @Test
    fun `songs sharing a position keep the order the query returned them in`() {
        val playlist = playlistWithSongs(
            songIds = listOf("a", "b", "c"),
            positions = mapOf("a" to 0, "b" to 0, "c" to 0),
        )

        assertThat(playlist.songsInPlaylistOrder().map { it.id }).containsExactly("a", "b", "c")
    }

    @Test
    fun `a song without a cross-ref sorts last instead of failing`() {
        val playlist = playlistWithSongs(
            songIds = listOf("orphan", "a"),
            positions = mapOf("a" to 5),
        )

        assertThat(playlist.songsInPlaylistOrder().map { it.id }).containsExactly("a", "orphan")
    }

    private fun playlistWithSongs(
        songIds: List<String>,
        positions: Map<String, Int>,
    ) = PlaylistWithSongs(
        playlist = PlaylistEntity(id = PLAYLIST_ID, name = "Playlist", createdAt = 0),
        songs = songIds.map { song(it) },
        crossRefs = positions.map { (songId, position) ->
            PlaylistSongCrossRef(
                playlistId = PLAYLIST_ID,
                songId = songId,
                addedAt = 0,
                position = position,
            )
        },
    )

    private fun song(id: String) = SongEntity(
        id = id,
        title = "Song $id",
        artistName = SongEntity.UNKNOWN_ARTIST,
        fileUri = "content://$id",
        imageUri = null,
        durationMillis = 60_000,
        isFavorite = false,
        createdAt = 0,
    )

    private companion object {
        const val PLAYLIST_ID = 1L
    }
}
