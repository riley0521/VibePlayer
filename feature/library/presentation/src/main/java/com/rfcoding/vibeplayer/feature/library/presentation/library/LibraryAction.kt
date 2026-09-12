package com.rfcoding.vibeplayer.feature.library.presentation.library

sealed interface LibraryAction {
    data object OnScanClick : LibraryAction
    data object OnScanAgainClick : LibraryAction
    data object OnSearchClick : LibraryAction
    data class OnTabSelect(val tab: LibraryTab) : LibraryAction
    data object OnShuffleClick : LibraryAction
    data object OnPlayClick : LibraryAction
    data class OnSongClick(val songId: String) : LibraryAction
    data object OnMiniPlayerClick : LibraryAction
    data object OnPlayPauseClick : LibraryAction
    data object OnSkipToPreviousClick : LibraryAction
    data object OnSkipNextClick : LibraryAction
    data class OnSeek(val fraction: Float) : LibraryAction
    data object OnCreatePlaylistClick : LibraryAction
    data object OnFavouritesClick : LibraryAction
    data object OnFavouritesMenuClick : LibraryAction
    data class OnPlaylistClick(val playlistId: Long) : LibraryAction
    data class OnPlaylistMenuClick(val playlistId: Long) : LibraryAction
}
