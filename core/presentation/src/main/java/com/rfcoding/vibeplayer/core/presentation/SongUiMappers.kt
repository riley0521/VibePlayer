package com.rfcoding.vibeplayer.core.presentation

import com.rfcoding.vibeplayer.core.domain.playlist.Playlist
import com.rfcoding.vibeplayer.core.domain.song.Song

fun Song.toSongUi(): SongUi = SongUi(
    id = id,
    title = title,
    artistName = artistName,
    imageUri = imageUri,
    durationMillis = durationMillis,
)

/** The cover the user picked wins; otherwise the first song that has artwork lends it. */
fun Playlist.toPlaylistUi(): PlaylistUi = PlaylistUi(
    id = id,
    name = name,
    songCount = songs.size,
    imageUri = coverUri ?: songs.firstNotNullOfOrNull { it.imageUri },
)
