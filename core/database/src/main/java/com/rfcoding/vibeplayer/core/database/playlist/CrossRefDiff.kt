package com.rfcoding.vibeplayer.core.database.playlist

import com.rfcoding.vibeplayer.core.database.entity.PlaylistSongCrossRef

/**
 * The cross-refs to add so [songIds] end up after the playlist's last song, keeping their order.
 * Songs already in the playlist are skipped, as is a repeated id within [songIds].
 */
fun appendCrossRefs(
    existing: List<PlaylistSongCrossRef>,
    playlistId: Long,
    songIds: List<String>,
    addedAt: Long,
): List<PlaylistSongCrossRef> {
    val existingIds = existing.mapTo(HashSet()) { it.songId }
    val nextPosition = (existing.maxOfOrNull { it.position } ?: -1) + 1
    return songIds
        .distinct()
        .filterNot { it in existingIds }
        .mapIndexed { index, songId ->
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                addedAt = addedAt,
                position = nextPosition + index,
            )
        }
}

data class CrossRefOrderDiff(
    val removedSongIds: List<String>,
    val upserts: List<PlaylistSongCrossRef>,
)

/**
 * Works out how to make the playlist hold exactly [songIds], in that order: the songs left out are
 * deleted and the rest are renumbered 0..n-1, keeping the date each one was added. An id that is not
 * in the playlist already is ignored, because the Edit playlist screen only ever removes and reorders.
 */
fun diffCrossRefOrder(
    existing: List<PlaylistSongCrossRef>,
    songIds: List<String>,
): CrossRefOrderDiff {
    val existingBySongId = existing.associateBy { it.songId }
    val keptIds = songIds.distinct().filter { it in existingBySongId }

    return CrossRefOrderDiff(
        removedSongIds = existing.map { it.songId } - keptIds.toSet(),
        upserts = keptIds.mapIndexed { index, songId ->
            existingBySongId.getValue(songId).copy(position = index)
        },
    )
}
