package com.rfcoding.vibeplayer.core.presentation.playlistname

sealed interface PlaylistNameAction {
    data class OnNameChange(val name: String) : PlaylistNameAction
    data object OnCancelClick : PlaylistNameAction
    data object OnConfirmClick : PlaylistNameAction
}
