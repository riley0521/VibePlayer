package com.rfcoding.vibeplayer.core.data.song

import com.rfcoding.vibeplayer.core.database.entity.SongEntity
import com.rfcoding.vibeplayer.core.domain.song.Song

fun SongEntity.toSong(): Song = Song(
    id = id,
    title = title,
    artistName = artistName.takeIf { it != SongEntity.UNKNOWN_ARTIST },
    fileUri = fileUri,
    imageUri = imageUri,
    durationMillis = durationMillis,
    isFavorite = isFavorite,
    createdAt = createdAt,
)

fun Song.toSongEntity(): SongEntity = SongEntity(
    id = id,
    title = title,
    artistName = artistName ?: SongEntity.UNKNOWN_ARTIST,
    fileUri = fileUri,
    imageUri = imageUri,
    durationMillis = durationMillis,
    isFavorite = isFavorite,
    createdAt = createdAt,
)
