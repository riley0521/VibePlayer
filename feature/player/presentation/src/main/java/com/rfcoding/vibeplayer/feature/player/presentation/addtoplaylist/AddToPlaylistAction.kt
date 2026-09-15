package com.rfcoding.vibeplayer.feature.player.presentation.addtoplaylist

sealed interface AddToPlaylistAction {
    data object OnCreatePlaylistClick : AddToPlaylistAction
    data object OnFavouritesClick : AddToPlaylistAction
    data class OnPlaylistClick(val playlistId: Long) : AddToPlaylistAction
}
