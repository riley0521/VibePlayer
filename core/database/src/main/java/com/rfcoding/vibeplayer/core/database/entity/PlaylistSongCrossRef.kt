package com.rfcoding.vibeplayer.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    // playlistId is covered by the primary key; songId needs its own index for the cascade.
    indices = [Index("songId")],
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: String,
    val addedAt: Long,
    /**
     * Added in version 3. The song's 0-based place in the playlist, as set on the Edit playlist
     * screen. Removals can leave gaps, because only the relative order is ever read.
     */
    @ColumnInfo(defaultValue = "0")
    val position: Int = 0,
)
