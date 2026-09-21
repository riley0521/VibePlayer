package com.rfcoding.vibeplayer.core.data.song

import com.rfcoding.vibeplayer.core.data.database.safeDatabaseUpdate
import com.rfcoding.vibeplayer.core.database.dao.SongDao
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class RoomSongDataSource(
    private val songDao: SongDao,
    private val applicationScope: CoroutineScope
) : SongLocalDataSource {

    override val songs: Flow<List<Song>> = songDao
        .observeSongs()
        .map { songs -> songs.map { it.toSong() } }
        .shareIn(applicationScope, SharingStarted.Lazily, replay = 1)

    override fun observeFavoriteSongs(): Flow<List<Song>> {
        return songDao.observeFavoriteSongs().map { songs -> songs.map { it.toSong() } }
    }

    override suspend fun setFavorite(songId: String, isFavorite: Boolean): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { songDao.setFavorite(songId, isFavorite) }
    }

    override suspend fun setFavorites(songIds: List<String>, isFavorite: Boolean): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { songDao.setFavorites(songIds, isFavorite) }
    }

    override suspend fun upsertScannedSongs(songs: List<Song>): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { songDao.upsertScannedSongs(songs.map { it.toSongEntity() }) }
    }

    override suspend fun pruneSongsMissingFrom(songs: List<Song>): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { songDao.pruneSongsMissingFrom(songs.map { it.toSongEntity() }) }
    }
}
