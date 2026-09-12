package com.rfcoding.vibeplayer.feature.library.presentation.library

import com.rfcoding.vibeplayer.feature.library.presentation.playlistname.PlaylistNameAction

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

    data object OnSheetDismiss : LibraryAction
    data object OnPlayFavouritesClick : LibraryAction
    data class OnPlayPlaylistClick(val playlistId: Long) : LibraryAction
    data class OnRenamePlaylistClick(val playlistId: Long) : LibraryAction
    data class OnChangePlaylistCoverClick(val playlistId: Long) : LibraryAction
    /** Opens the delete confirmation; [OnConfirmDeletePlaylistClick] is the one that deletes. */
    data class OnDeletePlaylistClick(val playlistId: Long) : LibraryAction
    data class OnConfirmDeletePlaylistClick(val playlistId: Long) : LibraryAction
    /** Forwards the name sheet's own actions so the screen keeps a single action funnel. */
    data class OnPlaylistNameAction(val action: PlaylistNameAction) : LibraryAction
}
