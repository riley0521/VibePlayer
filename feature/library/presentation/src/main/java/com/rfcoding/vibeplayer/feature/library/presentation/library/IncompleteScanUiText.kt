package com.rfcoding.vibeplayer.feature.library.presentation.library

import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.presentation.UiText
import com.rfcoding.vibeplayer.core.presentation.toUiText
import com.rfcoding.vibeplayer.feature.library.domain.IncompleteScan
import com.rfcoding.vibeplayer.feature.library.presentation.R

fun IncompleteScan.toUiText(): UiText = when {
    // Only the prune failed: every song was stored.
    skippedSongCount == 0 -> error.toUiText()
    error == DataError.Local.DISK_FULL -> UiText.PluralsResource(R.plurals.scan_songs_skipped_disk_full, skippedSongCount)
    else -> UiText.PluralsResource(R.plurals.scan_songs_skipped, skippedSongCount)
}
