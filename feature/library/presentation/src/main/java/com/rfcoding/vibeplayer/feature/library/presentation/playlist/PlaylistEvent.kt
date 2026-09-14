package com.rfcoding.vibeplayer.feature.library.presentation.playlist

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface PlaylistEvent {
    data object LaunchCoverPicker : PlaylistEvent
    data class Error(val message: UiText) : PlaylistEvent
}
