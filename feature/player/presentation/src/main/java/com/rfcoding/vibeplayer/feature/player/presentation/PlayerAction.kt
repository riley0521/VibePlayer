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
    data object OnSheetDismiss : PlayerAction
    /** The Create Playlist row of the add-to-playlist sheet. */
    data object OnCreatePlaylistClick : PlayerAction
    data class OnPlaylistCreated(val playlistId: Long, val name: String) : PlayerAction
    data object OnDownloadClick : PlayerAction
    /** The share card, already captured and encoded, is ready to be saved to the gallery. */
    class OnSaveCardClick(val pngBytes: ByteArray) : PlayerAction
    /** API 28 only: the user refused the storage permission that saving needs there. */
    data object OnStoragePermissionDenied : PlayerAction
}
