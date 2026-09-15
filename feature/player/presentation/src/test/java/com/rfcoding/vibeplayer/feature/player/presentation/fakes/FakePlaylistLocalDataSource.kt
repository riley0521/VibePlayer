package com.rfcoding.vibeplayer.feature.player.presentation.fakes

import com.rfcoding.vibeplayer.core.domain.playlist.Playlist
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePlaylistLocalDataSource : PlaylistLocalDataSource {
    val playlists = MutableStateFlow<List<Playlist>>(emptyList())

    /** When set, every write fails with it and leaves [playlists] untouched. */
    var error: DataError.Local? = null

    /** Song ids passed to [addSongsToPlaylist], by playlist; the fake has no songs to attach them to. */
    val addedSongIds = mutableMapOf<Long, List<String>>()

    override fun observePlaylists(): Flow<List<Playlist>> = playlists

    override fun observePlaylist(playlistId: Long): Flow<Playlist?> {
        return playlists.map { all -> all.find { it.id == playlistId } }
    }

    override suspend fun createPlaylist(name: String): Result<Long, DataError.Local> {
        error?.let { return Result.Error(it) }
        val id = (playlists.value.maxOfOrNull { it.id } ?: 0) + 1
        playlists.value = listOf(playlist(id, name)) + playlists.value
        return Result.Success(id)
    }

    override suspend fun renamePlaylist(playlistId: Long, name: String): EmptyResult<DataError.Local> {
        return update(playlistId) { it.copy(name = name) }
    }

    override suspend fun setPlaylistCover(playlistId: Long, coverUri: String): EmptyResult<DataError.Local> {
        return update(playlistId) { it.copy(coverUri = coverUri) }
    }

    override suspend fun deletePlaylist(playlistId: Long): EmptyResult<DataError.Local> {
        error?.let { return Result.Error(it) }
        playlists.value = playlists.value.filterNot { it.id == playlistId }
        return Result.Success(Unit)
    }

    override suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<String>): EmptyResult<DataError.Local> {
        error?.let { return Result.Error(it) }
        addedSongIds[playlistId] = addedSongIds[playlistId].orEmpty() + songIds
        return Result.Success(Unit)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String): EmptyResult<DataError.Local> {
        return Result.Success(Unit)
    }

    private fun update(playlistId: Long, transform: (Playlist) -> Playlist): EmptyResult<DataError.Local> {
        error?.let { return Result.Error(it) }
        playlists.value = playlists.value.map { if (it.id == playlistId) transform(it) else it }
        return Result.Success(Unit)
    }
}

fun playlist(
    id: Long,
    name: String = "Playlist $id",
    songs: List<Song> = emptyList(),
    coverUri: String? = null,
) = Playlist(id = id, name = name, createdAt = 0, songs = songs, coverUri = coverUri)
