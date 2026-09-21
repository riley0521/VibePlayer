package com.rfcoding.vibeplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.rfcoding.vibeplayer.core.database.entity.SongEntity
import com.rfcoding.vibeplayer.core.database.song.mergeScannedSongs
import com.rfcoding.vibeplayer.core.database.song.staleSongIds
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

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id IN (:songIds)")
    suspend fun setFavoriteByIds(songIds: List<String>, isFavorite: Boolean)

    @Transaction
    suspend fun setFavorites(songIds: List<String>, isFavorite: Boolean) {
        songIds.chunked(MAX_BIND_ARGS).forEach { setFavoriteByIds(it, isFavorite) }
    }

    /**
     * Upserts one batch of a scan. Songs already stored keep their id, favourite flag, creation
     * date and playlist links.
     */
    @Transaction
    suspend fun upsertScannedSongs(scanned: List<SongEntity>) {
        upsertSongs(mergeScannedSongs(existing = getAllSongs(), scanned = scanned))
    }

    /** Deletes the stored songs a full scan, [scanned], no longer found. */
    @Transaction
    suspend fun pruneSongsMissingFrom(scanned: List<SongEntity>) {
        // API 28's SQLite caps a statement at 999 bound variables.
        staleSongIds(existing = getAllSongs(), scanned = scanned)
            .chunked(MAX_BIND_ARGS)
            .forEach { deleteSongsByIds(it) }
    }
}

/** Keeps a statement's bound variables under API 28 SQLite's cap of 999. */
internal const val MAX_BIND_ARGS = 500
