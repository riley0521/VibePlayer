package com.rfcoding.vibeplayer.core.database.song

import com.rfcoding.vibeplayer.core.database.entity.SongEntity

/**
 * Works out the rows to upsert for one batch of a scan.
 *
 * Songs are matched by (title, artistName). A match keeps the stored id, favourite flag and
 * creation date (so playlist links survive) and takes the scanned file, artwork and duration.
 * When [scanned] holds the same key twice, the first one wins.
 */
fun mergeScannedSongs(
    existing: List<SongEntity>,
    scanned: List<SongEntity>,
): List<SongEntity> {
    val existingByKey = existing.associateBy { it.syncKey }
    return scanned
        .distinctBy { it.syncKey }
        .map { song ->
            val stored = existingByKey[song.syncKey] ?: return@map song
            song.copy(
                id = stored.id,
                isFavorite = stored.isFavorite,
                createdAt = stored.createdAt,
            )
        }
}

/** Ids of the [existing] rows that a full scan, [scanned], no longer found. */
fun staleSongIds(
    existing: List<SongEntity>,
    scanned: List<SongEntity>,
): List<String> {
    val scannedKeys = scanned.mapTo(HashSet()) { it.syncKey }
    return existing
        .filter { it.syncKey !in scannedKeys }
        .map { it.id }
}

private val SongEntity.syncKey: Pair<String, String>
    get() = title to artistName
