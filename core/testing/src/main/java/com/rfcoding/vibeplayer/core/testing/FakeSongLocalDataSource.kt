package com.rfcoding.vibeplayer.core.testing

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

    /** Set to make [setFavorite] and [setFavorites] fail without changing anything. */
    var setFavoriteError: DataError.Local? = null

    /** Set to make [syncScannedSongs] fail; the attempt is still recorded in [syncedSongs]. */
    var syncError: DataError.Local? = null

    /** Every list passed to [syncScannedSongs], in call order. */
    val syncedSongs = mutableListOf<List<Song>>()

    override fun observeFavoriteSongs(): Flow<List<Song>> = songsMutable.map { all -> all.filter { it.isFavorite } }

    override suspend fun setFavorite(songId: String, isFavorite: Boolean): EmptyResult<DataError.Local> {
        return setFavorites(listOf(songId), isFavorite)
    }

    override suspend fun setFavorites(songIds: List<String>, isFavorite: Boolean): EmptyResult<DataError.Local> {
        setFavoriteError?.let { return Result.Error(it) }
        songsMutable.value = songsMutable.value.map { if (it.id in songIds) it.copy(isFavorite = isFavorite) else it }
        return Result.Success(Unit)
    }

    override suspend fun syncScannedSongs(songs: List<Song>): EmptyResult<DataError.Local> {
        syncedSongs += songs
        syncError?.let { return Result.Error(it) }
        songsMutable.value = songs
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
