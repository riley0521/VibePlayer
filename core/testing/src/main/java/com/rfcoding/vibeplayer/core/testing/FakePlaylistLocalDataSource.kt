package com.rfcoding.vibeplayer.core.testing

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

    /**
     * Song ids passed to [addSongsToPlaylist], by playlist. The playlist also gains a `song(id)` for
     * every id it doesn't hold yet, so membership can be observed.
     */
    val addedSongIds = mutableMapOf<Long, List<String>>()

    /** Song ids passed to [setPlaylistSongs], by playlist, so tests can assert the saved order. */
    val setSongIds = mutableMapOf<Long, List<String>>()

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
        return update(playlistId) { playlist ->
            val heldIds = playlist.songs.map { it.id }.toSet()
            playlist.copy(songs = playlist.songs + songIds.filterNot { it in heldIds }.map { song(it) })
        }
    }

    override suspend fun removeSongsFromPlaylist(
        playlistId: Long,
        songIds: List<String>,
    ): EmptyResult<DataError.Local> {
        return update(playlistId) { playlist -> playlist.copy(songs = playlist.songs.filterNot { it.id in songIds }) }
    }

    override suspend fun setPlaylistSongs(playlistId: Long, songIds: List<String>): EmptyResult<DataError.Local> {
        val result = update(playlistId) { playlist ->
            val songsById = playlist.songs.associateBy { it.id }
            playlist.copy(songs = songIds.mapNotNull { songsById[it] })
        }
        if (result is Result.Success) setSongIds[playlistId] = songIds
        return result
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
