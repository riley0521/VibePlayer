package com.rfcoding.vibeplayer.core.presentation.playlistname

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface PlaylistNameEvent {
    data class PlaylistCreated(val playlistId: Long, val name: String) : PlaylistNameEvent
    data object PlaylistRenamed : PlaylistNameEvent
    data class Error(val message: UiText) : PlaylistNameEvent
}
