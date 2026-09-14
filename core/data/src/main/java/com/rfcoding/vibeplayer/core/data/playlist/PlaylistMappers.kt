package com.rfcoding.vibeplayer.core.data.playlist

import com.rfcoding.vibeplayer.core.data.song.toSong
import com.rfcoding.vibeplayer.core.database.entity.PlaylistWithSongs
import com.rfcoding.vibeplayer.core.domain.playlist.Playlist

fun PlaylistWithSongs.toPlaylist(): Playlist = Playlist(
    id = playlist.id,
    name = playlist.name,
    createdAt = playlist.createdAt,
    songs = songs.map { it.toSong() },
    coverUri = playlist.coverUri,
)
