package com.rfcoding.vibeplayer.feature.library.presentation.scan

import com.rfcoding.vibeplayer.feature.library.domain.MinDuration
import com.rfcoding.vibeplayer.feature.library.domain.MinSize
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters

/** Starts on the same filters the silent background scan uses. */
data class ScanMusicState(
    val minDuration: MinDuration = ScanFilters().minDuration,
    val minSize: MinSize = ScanFilters().minSize,
    val isScanning: Boolean = false,
)
