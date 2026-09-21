package com.rfcoding.vibeplayer.feature.library.domain

import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface MusicScanner {
    /**
     * Reads every `.mp3` directly inside the device's `Music/` folder that passes [filters] and has
     * a title tag, emitting the songs of every [SCAN_BATCH_SIZE] files as one batch. Songs sharing a
     * (title, artist) pair are emitted once across the whole scan. When the folder can't be listed,
     * emits a single error and completes.
     */
    fun scan(filters: ScanFilters): Flow<Result<List<ScannedSong>, DataError.Local>>
}

/** How many music files a scan reads before handing their songs over as one batch. */
const val SCAN_BATCH_SIZE = 100
