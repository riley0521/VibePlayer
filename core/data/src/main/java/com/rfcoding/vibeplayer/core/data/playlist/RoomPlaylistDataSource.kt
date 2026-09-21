package com.rfcoding.vibeplayer.core.data.playlist

import com.rfcoding.vibeplayer.core.data.database.safeDatabaseUpdate
import com.rfcoding.vibeplayer.core.database.dao.PlaylistDao
import com.rfcoding.vibeplayer.core.database.entity.PlaylistEntity
import com.rfcoding.vibeplayer.core.domain.playlist.Playlist
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class RoomPlaylistDataSource(
    private val playlistDao: PlaylistDao,
    private val clock: Clock = Clock.System,
) : PlaylistLocalDataSource {

    override fun observePlaylists(): Flow<List<Playlist>> {
        return playlistDao.observePlaylistsWithSongs().map { playlists -> playlists.map { it.toPlaylist() } }
    }

    override fun observePlaylist(playlistId: Long): Flow<Playlist?> {
        return playlistDao.observePlaylistWithSongs(playlistId).map { it?.toPlaylist() }
    }

    override suspend fun createPlaylist(name: String): Result<Long, DataError.Local> {
        return safeDatabaseUpdate {
            playlistDao.insertPlaylist(
                PlaylistEntity(name = name, createdAt = clock.now().toEpochMilliseconds()),
            )
        }
    }

    override suspend fun renamePlaylist(playlistId: Long, name: String): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { playlistDao.renamePlaylist(playlistId, name) }
    }

    override suspend fun setPlaylistCover(playlistId: Long, coverUri: String): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { playlistDao.setPlaylistCover(playlistId, coverUri) }
    }

    override suspend fun deletePlaylist(playlistId: Long): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { playlistDao.deletePlaylist(playlistId) }
    }

    override suspend fun addSongsToPlaylist(
        playlistId: Long,
        songIds: List<String>,
    ): EmptyResult<DataError.Local> {
        val addedAt = clock.now().toEpochMilliseconds()
        return safeDatabaseUpdate { playlistDao.addCrossRefs(playlistId, songIds, addedAt) }
    }

    override suspend fun removeSongsFromPlaylist(
        playlistId: Long,
        songIds: List<String>,
    ): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { playlistDao.deleteCrossRefs(playlistId, songIds) }
    }

    override suspend fun setPlaylistSongs(
        playlistId: Long,
        songIds: List<String>,
    ): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate { playlistDao.setCrossRefOrder(playlistId, songIds) }
    }
}
