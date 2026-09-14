package com.rfcoding.vibeplayer.feature.library.presentation.playlistname

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface PlaylistNameEvent {
    data class PlaylistCreated(val playlistId: Long) : PlaylistNameEvent
    data object PlaylistRenamed : PlaylistNameEvent
    data class Error(val message: UiText) : PlaylistNameEvent
}
