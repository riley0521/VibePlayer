package com.rfcoding.vibeplayer.core.domain.playlist

import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface PlaylistLocalDataSource {
    /** Newest first. */
    fun observePlaylists(): Flow<List<Playlist>>
    fun observePlaylist(playlistId: Long): Flow<Playlist?>

    /** Returns the new playlist's id. */
    suspend fun createPlaylist(name: String): Result<Long, DataError.Local>
    suspend fun deletePlaylist(playlistId: Long): EmptyResult<DataError.Local>

    /** Songs already in the playlist are left untouched. */
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<String>): EmptyResult<DataError.Local>
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String): EmptyResult<DataError.Local>
}
