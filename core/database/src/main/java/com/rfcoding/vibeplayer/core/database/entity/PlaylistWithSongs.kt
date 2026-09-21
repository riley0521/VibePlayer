package com.rfcoding.vibeplayer.core.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * A playlist and its songs. [songs] comes back in no particular order, because `@Relation` has no
 * `orderBy`, so [crossRefs] is fetched alongside it to carry each song's position: read the pair
 * through `songsInPlaylistOrder()` rather than using [songs] directly.
 */
data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId",
        ),
    )
    val songs: List<SongEntity>,
    @Relation(parentColumn = "id", entityColumn = "playlistId")
    val crossRefs: List<PlaylistSongCrossRef>,
)
