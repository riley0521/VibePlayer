package com.rfcoding.vibeplayer.feature.library.presentation.editplaylist

sealed interface EditPlaylistAction {
    data object OnBackClick : EditPlaylistAction
    data object OnSaveClick : EditPlaylistAction
    data class OnRemoveSongClick(val songId: String) : EditPlaylistAction
    data class OnMoveSong(val from: Int, val to: Int) : EditPlaylistAction

    data object OnConfirmDiscardClick : EditPlaylistAction
    data object OnDismissDiscardSheet : EditPlaylistAction
}
