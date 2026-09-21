package com.rfcoding.vibeplayer.feature.library.domain

import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface MusicLibraryRepository {

    /** Emits when a scan fails after [scanMusic] has already returned. */
    val incompleteScans: Flow<IncompleteScan>

    /**
     * Scans the `Music/` folder and makes the stored songs match what it found, one batch at a
     * time. Returns once the first batch holding songs is stored, with how many songs it stored,
     * or once the whole scan is done when no batch holds any; the remaining batches keep syncing in
     * the background. A scan started while another runs cancels it.
     *
     * A failure before that first batch leaves the stored songs untouched and is returned; a later
     * one is reported through [incompleteScans].
     */
    suspend fun scanMusic(filters: ScanFilters): Result<Int, DataError.Local>
}
