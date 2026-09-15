package com.rfcoding.vibeplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.rfcoding.vibeplayer.core.database.entity.PlaylistEntity
import com.rfcoding.vibeplayer.core.database.entity.PlaylistSongCrossRef
import com.rfcoding.vibeplayer.core.database.entity.PlaylistWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun observePlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, name: String)

    @Query("UPDATE playlists SET coverUri = :coverUri WHERE id = :playlistId")
    suspend fun setPlaylistCover(playlistId: Long, coverUri: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    /** A list insert already runs in one transaction; songs already in the playlist are ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(crossRefs: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId IN (:songIds)")
    suspend fun deleteCrossRefsByIds(playlistId: Long, songIds: List<String>)

    @Transaction
    suspend fun deleteCrossRefs(playlistId: Long, songIds: List<String>) {
        songIds.chunked(MAX_BIND_ARGS).forEach { deleteCrossRefsByIds(playlistId, it) }
    }
}
