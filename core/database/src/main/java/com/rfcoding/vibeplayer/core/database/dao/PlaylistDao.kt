package com.rfcoding.vibeplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.rfcoding.vibeplayer.core.database.entity.PlaylistEntity
import com.rfcoding.vibeplayer.core.database.entity.PlaylistSongCrossRef
import com.rfcoding.vibeplayer.core.database.entity.PlaylistWithSongs
import com.rfcoding.vibeplayer.core.database.playlist.appendCrossRefs
import com.rfcoding.vibeplayer.core.database.playlist.diffCrossRefOrder
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

    @Query("SELECT * FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getCrossRefs(playlistId: Long): List<PlaylistSongCrossRef>

    /** A list insert already runs in one transaction; songs already in the playlist are ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(crossRefs: List<PlaylistSongCrossRef>)

    @Upsert
    suspend fun upsertCrossRefs(crossRefs: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId IN (:songIds)")
    suspend fun deleteCrossRefsByIds(playlistId: Long, songIds: List<String>)

    @Transaction
    suspend fun deleteCrossRefs(playlistId: Long, songIds: List<String>) {
        songIds.chunked(MAX_BIND_ARGS).forEach { deleteCrossRefsByIds(playlistId, it) }
    }

    /** Appends [songIds] after the playlist's last song, so they get positions of their own. */
    @Transaction
    suspend fun addCrossRefs(playlistId: Long, songIds: List<String>, addedAt: Long) {
        val crossRefs = appendCrossRefs(
            existing = getCrossRefs(playlistId),
            playlistId = playlistId,
            songIds = songIds,
            addedAt = addedAt,
        )
        insertCrossRefs(crossRefs)
    }

    /** Leaves the playlist holding exactly [songIds], in that order. */
    @Transaction
    suspend fun setCrossRefOrder(playlistId: Long, songIds: List<String>) {
        val diff = diffCrossRefOrder(getCrossRefs(playlistId), songIds)
        deleteCrossRefs(playlistId, diff.removedSongIds)
        upsertCrossRefs(diff.upserts)
    }
}
