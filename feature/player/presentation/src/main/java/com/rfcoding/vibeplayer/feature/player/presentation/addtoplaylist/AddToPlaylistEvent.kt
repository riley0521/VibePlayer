package com.rfcoding.vibeplayer.feature.player.presentation.addtoplaylist

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface AddToPlaylistEvent {
    data class Error(val message: UiText) : AddToPlaylistEvent
}
