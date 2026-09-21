package com.rfcoding.vibeplayer.core.database.playlist

import com.rfcoding.vibeplayer.core.database.entity.PlaylistWithSongs
import com.rfcoding.vibeplayer.core.database.entity.SongEntity

/**
 * The playlist's songs in the order the user arranged them, since `@Relation` cannot order them
 * itself. A song whose cross-ref is somehow missing sorts last rather than failing; the sort is
 * stable, so songs sharing a position keep the order the query returned them in.
 */
fun PlaylistWithSongs.songsInPlaylistOrder(): List<SongEntity> {
    val positionBySongId = crossRefs.associate { it.songId to it.position }
    return songs.sortedBy { positionBySongId[it.id] ?: Int.MAX_VALUE }
}
