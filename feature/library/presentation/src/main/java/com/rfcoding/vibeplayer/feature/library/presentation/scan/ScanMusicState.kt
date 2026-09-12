package com.rfcoding.vibeplayer.feature.library.presentation.scan

import com.rfcoding.vibeplayer.feature.library.domain.MinDuration
import com.rfcoding.vibeplayer.feature.library.domain.MinSize

data class ScanMusicState(
    val minDuration: MinDuration = MinDuration.ThirtySeconds,
    val minSize: MinSize = MinSize.FiveHundredKb,
    val isScanning: Boolean = false,
)
