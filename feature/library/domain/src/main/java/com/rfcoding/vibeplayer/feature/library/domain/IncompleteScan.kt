package com.rfcoding.vibeplayer.feature.library.domain

import com.rfcoding.vibeplayer.core.domain.util.DataError

/**
 * A scan that failed after its first batch was stored: the stored songs were kept but not pruned,
 * and [skippedSongCount] scanned songs couldn't be stored because of [error].
 */
data class IncompleteScan(
    val skippedSongCount: Int,
    val error: DataError.Local,
)
