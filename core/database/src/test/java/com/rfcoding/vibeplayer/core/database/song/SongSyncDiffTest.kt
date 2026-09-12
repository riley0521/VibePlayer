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

        val diff = diffScannedSongs(existing = emptyList(), scanned = listOf(scanned))

        assertThat(diff.upserts).containsExactly(scanned)
        assertThat(diff.deletedIds).isEmpty()
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

        val diff = diffScannedSongs(existing = listOf(stored), scanned = listOf(scanned))

        assertThat(diff.upserts).containsExactly(
            scanned.copy(id = "stored", isFavorite = true, createdAt = 1),
        )
        assertThat(diff.deletedIds).isEmpty()
    }

    @Test
    fun `stored songs the scan no longer found are deleted`() {
        val kept = song(id = "kept", title = "Kept")
        val gone = song(id = "gone", title = "Gone")

        val diff = diffScannedSongs(
            existing = listOf(kept, gone),
            scanned = listOf(song(id = "fresh", title = "Kept")),
        )

        assertThat(diff.upserts.map { it.id }).containsExactly("kept")
        assertThat(diff.deletedIds).containsExactly("gone")
    }

    @Test
    fun `an empty scan deletes every stored song`() {
        val diff = diffScannedSongs(
            existing = listOf(song(id = "a", title = "A"), song(id = "b", title = "B")),
            scanned = emptyList(),
        )

        assertThat(diff.upserts).isEmpty()
        assertThat(diff.deletedIds).containsExactlyInAnyOrder("a", "b")
    }

    @Test
    fun `when the scan holds the same title and artist twice, the first one wins`() {
        val first = song(id = "first", title = "Intro", artistName = "Band", fileUri = "content://1")
        val second = song(id = "second", title = "Intro", artistName = "Band", fileUri = "content://2")

        val diff = diffScannedSongs(existing = emptyList(), scanned = listOf(first, second))

        assertThat(diff.upserts).containsExactly(first)
    }

    @Test
    fun `the same title by different artists are different songs`() {
        val stored = song(id = "stored", title = "Intro", artistName = SongEntity.UNKNOWN_ARTIST)
        val byBand = song(id = "band", title = "Intro", artistName = "Band")

        val diff = diffScannedSongs(existing = listOf(stored), scanned = listOf(byBand))

        assertThat(diff.upserts).containsExactly(byBand)
        assertThat(diff.deletedIds).containsExactly("stored")
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
