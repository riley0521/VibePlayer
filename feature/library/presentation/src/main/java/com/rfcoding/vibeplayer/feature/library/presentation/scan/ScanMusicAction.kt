package com.rfcoding.vibeplayer.feature.library.presentation.scan

import com.rfcoding.vibeplayer.feature.library.domain.MinDuration
import com.rfcoding.vibeplayer.feature.library.domain.MinSize

sealed interface ScanMusicAction {
    data object OnBackClick : ScanMusicAction
    data class OnMinDurationSelect(val minDuration: MinDuration) : ScanMusicAction
    data class OnMinSizeSelect(val minSize: MinSize) : ScanMusicAction
    data object OnScanClick : ScanMusicAction
}
