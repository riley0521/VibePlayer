package com.rfcoding.vibeplayer.feature.library.presentation.editplaylist

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface EditPlaylistEvent {
    data object NavigateBack : EditPlaylistEvent
    data class Error(val message: UiText) : EditPlaylistEvent
}
