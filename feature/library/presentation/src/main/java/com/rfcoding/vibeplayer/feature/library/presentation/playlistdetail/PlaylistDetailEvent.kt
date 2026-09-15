package com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface PlaylistDetailEvent {
    /** The playlist no longer exists, so there is nothing left to show. */
    data object NavigateBack : PlaylistDetailEvent
    data class Error(val message: UiText) : PlaylistDetailEvent
}
