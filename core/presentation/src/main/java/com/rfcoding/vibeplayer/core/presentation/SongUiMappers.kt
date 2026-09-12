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

/** The cover is the first song's artwork until playlists get a cover of their own. */
fun Playlist.toPlaylistUi(): PlaylistUi = PlaylistUi(
    id = id,
    name = name,
    songCount = songs.size,
    imageUri = songs.firstNotNullOfOrNull { it.imageUri },
)
