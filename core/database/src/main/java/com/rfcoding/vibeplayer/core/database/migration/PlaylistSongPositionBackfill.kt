package com.rfcoding.vibeplayer.core.database.migration

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 3 adds `playlist_song_cross_ref.position`, which the auto migration fills with 0 for every
 * existing row. This numbers those rows per playlist instead, oldest link first, so playlists keep a
 * stable order after the update. Ties fall back to the song id to stay deterministic.
 *
 * The counting subquery replaces a window function, which SQLite on API 28 does not have.
 */
class PlaylistSongPositionBackfill : AutoMigrationSpec {

    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            UPDATE playlist_song_cross_ref
            SET position = (
                SELECT COUNT(*) FROM playlist_song_cross_ref AS other
                WHERE other.playlistId = playlist_song_cross_ref.playlistId
                  AND (other.addedAt < playlist_song_cross_ref.addedAt
                       OR (other.addedAt = playlist_song_cross_ref.addedAt
                           AND other.songId < playlist_song_cross_ref.songId))
            )
            """.trimIndent(),
        )
    }
}
