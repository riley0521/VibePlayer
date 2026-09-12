package com.rfcoding.vibeplayer.feature.library.presentation.playlistname

sealed interface PlaylistNameAction {
    data class OnNameChange(val name: String) : PlaylistNameAction
    data object OnCancelClick : PlaylistNameAction
    data object OnConfirmClick : PlaylistNameAction
}
