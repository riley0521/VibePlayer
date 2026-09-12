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

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    /** A list insert already runs in one transaction; songs already in the playlist are ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(crossRefs: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteCrossRef(playlistId: Long, songId: String)
}
