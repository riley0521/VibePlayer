package com.rfcoding.vibeplayer.feature.library.presentation.fakes

import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSongLocalDataSource : SongLocalDataSource {
    val songsMutable = MutableStateFlow<List<Song>>(emptyList())
    override val songs: Flow<List<Song>> = songsMutable

    override fun observeFavoriteSongs(): Flow<List<Song>> = songsMutable.map { all -> all.filter { it.isFavorite } }

    override suspend fun setFavorite(songId: String, isFavorite: Boolean): EmptyResult<DataError.Local> {
        songsMutable.value = songsMutable.value.map { if (it.id == songId) it.copy(isFavorite = isFavorite) else it }
        return Result.Success(Unit)
    }

    override suspend fun syncScannedSongs(songs: List<Song>): EmptyResult<DataError.Local> {
        this.songsMutable.value = songs
        return Result.Success(Unit)
    }
}

fun song(id: String, isFavorite: Boolean = false) = Song(
    id = id,
    title = "Song $id",
    artistName = null,
    fileUri = "content://$id",
    imageUri = null,
    durationMillis = 60_000,
    isFavorite = isFavorite,
    createdAt = 0,
)
