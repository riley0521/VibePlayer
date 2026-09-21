package com.rfcoding.vibeplayer.core.database.song

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import com.rfcoding.vibeplayer.core.database.entity.SongEntity
import org.junit.jupiter.api.Test

class SongSyncDiffTest {

    @Test
    fun `a song new to the library is inserted as scanned`() {
        val scanned = song(id = "new", title = "Intro")

        val upserts = mergeScannedSongs(existing = emptyList(), scanned = listOf(scanned))

        assertThat(upserts).containsExactly(scanned)
    }

    @Test
    fun `a stored song keeps its id, favourite flag and date but takes the scanned file`() {
        val stored = song(
            id = "stored",
            title = "Intro",
            artistName = "Band",
            fileUri = "content://old",
            imageUri = null,
            durationMillis = 60_000,
            isFavorite = true,
            createdAt = 1,
        )
        val scanned = song(
            id = "fresh",
            title = "Intro",
            artistName = "Band",
            fileUri = "content://new",
            imageUri = "file://cover",
            durationMillis = 61_000,
            isFavorite = false,
            createdAt = 2,
        )

        val upserts = mergeScannedSongs(existing = listOf(stored), scanned = listOf(scanned))

        assertThat(upserts).containsExactly(
            scanned.copy(id = "stored", isFavorite = true, createdAt = 1),
        )
        assertThat(staleSongIds(existing = listOf(stored), scanned = listOf(scanned))).isEmpty()
    }

    @Test
    fun `stored songs the scan no longer found are deleted`() {
        val kept = song(id = "kept", title = "Kept")
        val gone = song(id = "gone", title = "Gone")

        val staleIds = staleSongIds(
            existing = listOf(kept, gone),
            scanned = listOf(song(id = "fresh", title = "Kept")),
        )

        assertThat(staleIds).containsExactly("gone")
    }

    @Test
    fun `an empty scan deletes every stored song`() {
        val staleIds = staleSongIds(
            existing = listOf(song(id = "a", title = "A"), song(id = "b", title = "B")),
            scanned = emptyList(),
        )

        assertThat(staleIds).containsExactlyInAnyOrder("a", "b")
    }

    @Test
    fun `when the scan holds the same title and artist twice, the first one wins`() {
        val first = song(id = "first", title = "Intro", artistName = "Band", fileUri = "content://1")
        val second = song(id = "second", title = "Intro", artistName = "Band", fileUri = "content://2")

        val upserts = mergeScannedSongs(existing = emptyList(), scanned = listOf(first, second))

        assertThat(upserts).containsExactly(first)
    }

    @Test
    fun `the same title by different artists are different songs`() {
        val stored = song(id = "stored", title = "Intro", artistName = SongEntity.UNKNOWN_ARTIST)
        val byBand = song(id = "band", title = "Intro", artistName = "Band")

        assertThat(mergeScannedSongs(existing = listOf(stored), scanned = listOf(byBand))).containsExactly(byBand)
        assertThat(staleSongIds(existing = listOf(stored), scanned = listOf(byBand))).containsExactly("stored")
    }

    private fun song(
        id: String,
        title: String,
        artistName: String = SongEntity.UNKNOWN_ARTIST,
        fileUri: String = "content://$id",
        imageUri: String? = null,
        durationMillis: Long = 60_000,
        isFavorite: Boolean = false,
        createdAt: Long = 0,
    ) = SongEntity(
        id = id,
        title = title,
        artistName = artistName,
        fileUri = fileUri,
        imageUri = imageUri,
        durationMillis = durationMillis,
        isFavorite = isFavorite,
        createdAt = createdAt,
    )
}
