package com.rfcoding.vibeplayer.feature.player.presentation

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface PlayerEvent {
    data class AddedToPlaylist(val playlistName: UiText) : PlayerEvent
    data class Error(val message: UiText) : PlayerEvent
}
