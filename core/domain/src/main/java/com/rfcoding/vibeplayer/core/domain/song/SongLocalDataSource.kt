package com.rfcoding.vibeplayer.core.domain.song

import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface SongLocalDataSource {
    fun observeSongs(): Flow<List<Song>>
    fun observeFavoriteSongs(): Flow<List<Song>>
    suspend fun setFavorite(songId: String, isFavorite: Boolean): EmptyResult<DataError.Local>

    /**
     * Makes the stored songs match [songs], the result of a full scan: songs already stored keep
     * their id, favourite flag, creation date and playlist links; songs missing from [songs] are
     * deleted.
     */
    suspend fun syncScannedSongs(songs: List<Song>): EmptyResult<DataError.Local>
}
