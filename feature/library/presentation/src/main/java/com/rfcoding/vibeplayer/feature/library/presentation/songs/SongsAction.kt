package com.rfcoding.vibeplayer.feature.library.presentation.songs

sealed interface SongsAction {
    data object OnShuffleClick : SongsAction
    data object OnPlayClick : SongsAction
    data class OnSongClick(val songId: String) : SongsAction
}