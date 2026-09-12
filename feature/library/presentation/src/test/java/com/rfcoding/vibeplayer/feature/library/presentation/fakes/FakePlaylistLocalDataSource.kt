package com.rfcoding.vibeplayer.feature.library.presentation.fakes

import com.rfcoding.vibeplayer.core.domain.playlist.Playlist
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePlaylistLocalDataSource : PlaylistLocalDataSource {
    val playlists = MutableStateFlow<List<Playlist>>(emptyList())

    override fun observePlaylists(): Flow<List<Playlist>> = playlists

    override fun observePlaylist(playlistId: Long): Flow<Playlist?> {
        return playlists.map { all -> all.find { it.id == playlistId } }
    }

    override suspend fun createPlaylist(name: String): Result<Long, DataError.Local> {
        val id = (playlists.value.maxOfOrNull { it.id } ?: 0) + 1
        playlists.value = listOf(Playlist(id, name, createdAt = 0, songs = emptyList())) + playlists.value
        return Result.Success(id)
    }

    override suspend fun deletePlaylist(playlistId: Long): EmptyResult<DataError.Local> {
        playlists.value = playlists.value.filterNot { it.id == playlistId }
        return Result.Success(Unit)
    }

    override suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<String>): EmptyResult<DataError.Local> {
        return Result.Success(Unit)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String): EmptyResult<DataError.Local> {
        return Result.Success(Unit)
    }
}
