package com.rfcoding.vibeplayer.feature.library.presentation.fakes

import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.library.domain.MusicLibraryRepository
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import kotlinx.coroutines.CompletableDeferred

class FakeMusicLibraryRepository : MusicLibraryRepository {
    var result: Result<Int, DataError.Local> = Result.Success(0)
    /** When set, a scan suspends until it completes, which keeps the scan "running". */
    var gate: CompletableDeferred<Unit>? = null
    /** Runs when a scan succeeds, standing in for the sync writing to the database. */
    var onScanSuccess: () -> Unit = {}
    val receivedFilters = mutableListOf<ScanFilters>()

    override suspend fun scanMusic(filters: ScanFilters): Result<Int, DataError.Local> {
        receivedFilters += filters
        gate?.await()
        if (result is Result.Success) onScanSuccess()
        return result
    }
}
