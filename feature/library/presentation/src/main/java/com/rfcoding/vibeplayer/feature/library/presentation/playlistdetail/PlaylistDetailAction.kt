package com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail

sealed interface PlaylistDetailAction {
    data object OnBackClick : PlaylistDetailAction
    data object OnAddSongsClick : PlaylistDetailAction
    data object OnShuffleClick : PlaylistDetailAction
    data object OnPlayClick : PlaylistDetailAction
    data class OnSongClick(val songId: String) : PlaylistDetailAction
}
