package com.rfcoding.vibeplayer.feature.library.domain

import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result

interface MusicScanner {
    /**
     * Reads every `.mp3` directly inside the device's `Music/` folder that passes [filters] and has
     * a title tag. Songs sharing a (title, artist) pair are returned once.
     */
    suspend fun scan(filters: ScanFilters): Result<List<ScannedSong>, DataError.Local>
}
