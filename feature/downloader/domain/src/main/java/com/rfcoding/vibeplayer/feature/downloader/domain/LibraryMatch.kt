package com.rfcoding.vibeplayer.feature.downloader.domain

import com.rfcoding.vibeplayer.core.domain.song.Song

/**
 * A song's identity for "already downloaded": title and artist, trimmed and case-insensitive, with
 * no artist the same as an empty one.
 */
data class LibraryKey(val title: String, val artistName: String)

fun libraryKey(title: String, artistName: String?) = LibraryKey(
    title = title.trim().lowercase(),
    artistName = artistName?.trim()?.lowercase().orEmpty(),
)

fun List<Song>.toLibraryKeys(): Set<LibraryKey> = mapTo(HashSet()) { libraryKey(it.title, it.artistName) }

fun RemoteTrack.isIn(libraryKeys: Set<LibraryKey>): Boolean = libraryKey(title, artistName) in libraryKeys

/**
 * What "Download all" enqueues: songs the library doesn't have yet that aren't already queued or
 * downloading. Failed ones are tried again. A playlist listing the same song twice gets it once.
 */
fun tracksToDownload(
    tracks: List<RemoteTrack>,
    libraryKeys: Set<LibraryKey>,
    statuses: Map<String, DownloadStatus>,
): List<RemoteTrack> = tracks
    .filter { !it.isIn(libraryKeys) }
    .filter { statuses[it.videoId] == null || statuses[it.videoId] is DownloadStatus.Failed }
    .distinctBy { libraryKey(it.title, it.artistName) }
