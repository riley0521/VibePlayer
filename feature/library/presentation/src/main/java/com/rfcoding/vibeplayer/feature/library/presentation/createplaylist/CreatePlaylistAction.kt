package com.rfcoding.vibeplayer.feature.library.presentation.createplaylist

sealed interface CreatePlaylistAction {
    data class OnNameChange(val name: String) : CreatePlaylistAction
    data object OnCancelClick : CreatePlaylistAction
    data object OnCreateClick : CreatePlaylistAction
}
