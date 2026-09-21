package com.rfcoding.vibeplayer.core.database.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.rfcoding.vibeplayer.core.database.VibePlayerDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The 2 → 3 auto migration adds `playlist_song_cross_ref.position` filled with 0, and
 * [PlaylistSongPositionBackfill] then numbers the existing links. Only real SQLite can tell whether
 * its counting subquery is right, so this runs the migration for real instead of imitating it.
 *
 * `runMigrationsAndValidate` also proves the auto migration and its spec are registered at all, and
 * that what comes out matches the exported schema 3.
 */
@RunWith(AndroidJUnit4::class)
class PlaylistSongPositionBackfillTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        VibePlayerDatabase::class.java,
    )

    @Test
    fun songsAreNumberedFromZeroOldestLinkFirst() {
        // Inserted out of order, so a migration that just kept the rows as they came would fail.
        seedVersion2 {
            insertPlaylist(PLAYLIST)
            insertCrossRef(PLAYLIST, songId = "b", addedAt = 200)
            insertCrossRef(PLAYLIST, songId = "c", addedAt = 300)
            insertCrossRef(PLAYLIST, songId = "a", addedAt = 100)
        }

        val db = migrateToVersion3()

        assertThat(db.positionsOf(PLAYLIST)).isEqualTo(mapOf("a" to 0, "b" to 1, "c" to 2))
    }

    @Test
    fun aSingleSongGetsPositionZero() {
        seedVersion2 {
            insertPlaylist(PLAYLIST)
            insertCrossRef(PLAYLIST, songId = "a", addedAt = 100)
        }

        val db = migrateToVersion3()

        assertThat(db.positionsOf(PLAYLIST)).isEqualTo(mapOf("a" to 0))
    }

    @Test
    fun everyPlaylistIsNumberedOnItsOwn() {
        seedVersion2 {
            insertPlaylist(PLAYLIST)
            insertPlaylist(OTHER_PLAYLIST)
            insertCrossRef(PLAYLIST, songId = "a", addedAt = 100)
            insertCrossRef(PLAYLIST, songId = "b", addedAt = 300)
            insertCrossRef(OTHER_PLAYLIST, songId = "c", addedAt = 200)
            insertCrossRef(OTHER_PLAYLIST, songId = "d", addedAt = 400)
        }

        val db = migrateToVersion3()

        // Both start at 0; a subquery that forgot to correlate on playlistId would number 0..3.
        assertThat(db.positionsOf(PLAYLIST)).isEqualTo(mapOf("a" to 0, "b" to 1))
        assertThat(db.positionsOf(OTHER_PLAYLIST)).isEqualTo(mapOf("c" to 0, "d" to 1))
    }

    @Test
    fun songsAddedInTheSameMillisecondFallBackToTheSongId() {
        seedVersion2 {
            insertPlaylist(PLAYLIST)
            insertCrossRef(PLAYLIST, songId = "c", addedAt = 100)
            insertCrossRef(PLAYLIST, songId = "a", addedAt = 100)
            insertCrossRef(PLAYLIST, songId = "b", addedAt = 100)
        }

        val db = migrateToVersion3()

        // Counting on addedAt alone would leave all three sharing position 0.
        assertThat(db.positionsOf(PLAYLIST)).isEqualTo(mapOf("a" to 0, "b" to 1, "c" to 2))
    }

    @Test
    fun aDatabaseWithNoPlaylistLinksMigratesCleanly() {
        seedVersion2 { }

        val db = migrateToVersion3()

        assertThat(db.positionsOf(PLAYLIST)).isEmpty()
    }

    /**
     * Opens the database at version 2, fills it in and closes it, which is what
     * [migrateToVersion3] then upgrades.
     */
    private fun seedVersion2(seed: SupportSQLiteDatabase.() -> Unit) {
        helper.createDatabase(TEST_DB, 2).use { db ->
            db.seed()
        }
    }

    private fun migrateToVersion3(): SupportSQLiteDatabase {
        return helper.runMigrationsAndValidate(TEST_DB, 3, true)
    }

    private fun SupportSQLiteDatabase.insertPlaylist(id: Long) {
        execSQL(
            "INSERT INTO playlists (id, name, createdAt, coverUri) VALUES (?, ?, ?, NULL)",
            arrayOf<Any>(id, "Playlist $id", 0L),
        )
    }

    /** The song rows the cross-refs point at; `songs` is unique on (title, artistName). */
    private fun SupportSQLiteDatabase.insertSong(id: String) {
        execSQL(
            """
            INSERT INTO songs (id, title, artistName, fileUri, imageUri, durationMillis, isFavorite, createdAt)
            VALUES (?, ?, '', ?, NULL, 60000, 0, 0)
            """.trimIndent(),
            arrayOf(id, "Song $id", "content://$id"),
        )
    }

    /** Version 2 has no `position` column yet; that is what the migration adds. */
    private fun SupportSQLiteDatabase.insertCrossRef(playlistId: Long, songId: String, addedAt: Long) {
        insertSong(songId)
        execSQL(
            "INSERT INTO playlist_song_cross_ref (playlistId, songId, addedAt) VALUES (?, ?, ?)",
            arrayOf<Any>(playlistId, songId, addedAt),
        )
    }

    /** Song id to position, read back in position order so a wrong order fails the map comparison. */
    private fun SupportSQLiteDatabase.positionsOf(playlistId: Long): Map<String, Int> {
        val positions = linkedMapOf<String, Int>()
        query(
            "SELECT songId, position FROM playlist_song_cross_ref WHERE playlistId = ? ORDER BY position",
            arrayOf(playlistId),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                positions[cursor.getString(0)] = cursor.getInt(1)
            }
        }
        return positions
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
        const val PLAYLIST = 1L
        const val OTHER_PLAYLIST = 2L
    }
}
