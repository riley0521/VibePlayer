package com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail

sealed interface PlaylistDetailEvent {
    /** The playlist no longer exists, so there is nothing left to show. */
    data object NavigateBack : PlaylistDetailEvent
}
