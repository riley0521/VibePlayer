package com.rfcoding.vibeplayer.core.database.song

import com.rfcoding.vibeplayer.core.database.entity.SongEntity

data class SongSyncDiff(
    val upserts: List<SongEntity>,
    val deletedIds: List<String>,
)

/**
 * Works out how to turn [existing] rows into the result of a full scan.
 *
 * Songs are matched by (title, artistName). A match keeps the stored id, favourite flag and
 * creation date (so playlist links survive) and takes the scanned file, artwork and duration.
 * Stored songs the scan no longer found are deleted. When [scanned] holds the same key twice, the
 * first one wins.
 */
fun diffScannedSongs(
    existing: List<SongEntity>,
    scanned: List<SongEntity>,
): SongSyncDiff {
    val existingByKey = existing.associateBy { it.syncKey }
    val uniqueScanned = scanned.distinctBy { it.syncKey }

    val upserts = uniqueScanned.map { song ->
        val stored = existingByKey[song.syncKey] ?: return@map song
        song.copy(
            id = stored.id,
            isFavorite = stored.isFavorite,
            createdAt = stored.createdAt,
        )
    }
    val scannedKeys = uniqueScanned.mapTo(HashSet()) { it.syncKey }
    val deletedIds = existing
        .filter { it.syncKey !in scannedKeys }
        .map { it.id }

    return SongSyncDiff(upserts = upserts, deletedIds = deletedIds)
}

private val SongEntity.syncKey: Pair<String, String>
    get() = title to artistName
