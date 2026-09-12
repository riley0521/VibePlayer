package com.rfcoding.vibeplayer.feature.library.domain

import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result

interface MusicLibraryRepository {
    /**
     * Scans the `Music/` folder and makes the stored songs match what it found. Returns how many
     * songs the library holds afterwards. A failed scan leaves the stored songs untouched.
     */
    suspend fun scanMusic(filters: ScanFilters): Result<Int, DataError.Local>
}
