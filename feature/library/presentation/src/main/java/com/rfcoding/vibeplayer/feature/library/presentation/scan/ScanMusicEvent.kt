package com.rfcoding.vibeplayer.feature.library.presentation.scan

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface ScanMusicEvent {
    data object NavigateBack : ScanMusicEvent
    data class Error(val message: UiText) : ScanMusicEvent
}
