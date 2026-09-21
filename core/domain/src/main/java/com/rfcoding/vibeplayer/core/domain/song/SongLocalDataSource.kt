package com.rfcoding.vibeplayer.core.domain.song

import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface SongLocalDataSource {

    /**
     * This will be a SharedFlow because LibraryScreen and SongsScreen will observe this.
     * We need shared instance of this flow.
     */
    val songs: Flow<List<Song>>
    fun observeFavoriteSongs(): Flow<List<Song>>
    suspend fun setFavorite(songId: String, isFavorite: Boolean): EmptyResult<DataError.Local>

    /** Sets [isFavorite] on every song in [songIds] in one transaction. */
    suspend fun setFavorites(songIds: List<String>, isFavorite: Boolean): EmptyResult<DataError.Local>

    /**
     * Stores one batch of scanned songs. Songs already stored keep their id, favourite flag,
     * creation date and playlist links.
     */
    suspend fun upsertScannedSongs(songs: List<Song>): EmptyResult<DataError.Local>

    /** Deletes the stored songs missing from [songs], the result of a full scan. */
    suspend fun pruneSongsMissingFrom(songs: List<Song>): EmptyResult<DataError.Local>
}
