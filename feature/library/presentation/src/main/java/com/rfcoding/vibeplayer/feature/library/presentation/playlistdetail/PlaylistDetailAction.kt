package com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail

sealed interface PlaylistDetailAction {
    data object OnBackClick : PlaylistDetailAction
    data object OnAddSongsClick : PlaylistDetailAction
    data object OnShuffleClick : PlaylistDetailAction
    data object OnPlayClick : PlaylistDetailAction
    data class OnSongClick(val songId: String) : PlaylistDetailAction

    data object OnEditClick : PlaylistDetailAction
    data object OnExitDeleteModeClick : PlaylistDetailAction
    data class OnSelectAllChange(val selected: Boolean) : PlaylistDetailAction
    data class OnSongSelectedChange(val songId: String, val selected: Boolean) : PlaylistDetailAction
    data object OnDeleteSelectedClick : PlaylistDetailAction
    data object OnConfirmRemoveClick : PlaylistDetailAction
    data object OnDismissRemoveSheet : PlaylistDetailAction
}
