package com.rfcoding.vibeplayer.core.data.song

import com.rfcoding.vibeplayer.core.data.database.safeDatabaseUpdate
import com.rfcoding.vibeplayer.core.database.dao.SongDao
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSongDataSource(
    private val songDao: SongDao,
) : SongLocalDataSource {

    override fun observeSongs(): Flow<List<Song>> {
        return songDao.observeSongs().map { songs -> songs.map { it.toSong() } }
    }

    override fun observeFavoriteSongs(): Flow<List<Song>> {
        return songDao.observeFavoriteSongs().map { songs -> songs.map { it.toSong() } }
    }

    override suspend fun setFavorite(songId: String, isFavorite: Boolean): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { songDao.setFavorite(songId, isFavorite) }
    }

    override suspend fun syncScannedSongs(songs: List<Song>): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { songDao.syncScannedSongs(songs.map { it.toSongEntity() }) }
    }
}
