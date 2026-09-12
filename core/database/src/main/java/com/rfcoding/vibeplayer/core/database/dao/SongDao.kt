package com.rfcoding.vibeplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.rfcoding.vibeplayer.core.database.entity.SongEntity
import com.rfcoding.vibeplayer.core.database.song.diffScannedSongs
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE")
    fun observeSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE")
    fun observeFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsByIds(ids: List<String>)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun setFavorite(songId: String, isFavorite: Boolean)

    /** Upserts the result of a full scan and prunes the songs it no longer found, atomically. */
    @Transaction
    suspend fun syncScannedSongs(scanned: List<SongEntity>) {
        val diff = diffScannedSongs(existing = getAllSongs(), scanned = scanned)
        upsertSongs(diff.upserts)
        // API 28's SQLite caps a statement at 999 bound variables.
        diff.deletedIds.chunked(MAX_BIND_ARGS).forEach { deleteSongsByIds(it) }
    }
}

private const val MAX_BIND_ARGS = 500
