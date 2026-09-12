package com.rfcoding.vibeplayer.feature.player.presentation

sealed interface PlayerAction {
    data object OnBackClick : PlayerAction
    data object OnAddToPlaylistClick : PlayerAction
    data object OnFavoriteClick : PlayerAction
    data object OnShuffleClick : PlayerAction
    data object OnRepeatClick : PlayerAction
    data object OnPreviousClick : PlayerAction
    data object OnPlayPauseClick : PlayerAction
    data object OnNextClick : PlayerAction
    data class OnSeek(val fraction: Float) : PlayerAction
}
