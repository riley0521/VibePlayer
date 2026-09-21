package com.rfcoding.vibeplayer.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.rfcoding.vibeplayer.core.database.dao.PlaylistDao
import com.rfcoding.vibeplayer.core.database.dao.SongDao
import com.rfcoding.vibeplayer.core.database.entity.PlaylistEntity
import com.rfcoding.vibeplayer.core.database.entity.PlaylistSongCrossRef
import com.rfcoding.vibeplayer.core.database.entity.SongEntity
import com.rfcoding.vibeplayer.core.database.migration.PlaylistSongPositionBackfill

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        // 2 adds the nullable playlists.coverUri column.
        AutoMigration(from = 1, to = 2),
        // 3 adds playlist_song_cross_ref.position; the spec numbers the existing links.
        AutoMigration(from = 2, to = 3, spec = PlaylistSongPositionBackfill::class),
    ],
)
abstract class VibePlayerDatabase : RoomDatabase() {
    abstract val songDao: SongDao
    abstract val playlistDao: PlaylistDao

    companion object {
        const val NAME = "vibeplayer.db"
    }
}
