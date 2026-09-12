package com.rfcoding.vibeplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rfcoding.vibeplayer.core.database.dao.PlaylistDao
import com.rfcoding.vibeplayer.core.database.dao.SongDao
import com.rfcoding.vibeplayer.core.database.entity.PlaylistEntity
import com.rfcoding.vibeplayer.core.database.entity.PlaylistSongCrossRef
import com.rfcoding.vibeplayer.core.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class VibePlayerDatabase : RoomDatabase() {
    abstract val songDao: SongDao
    abstract val playlistDao: PlaylistDao

    companion object {
        const val NAME = "vibeplayer.db"
    }
}
