package com.rfcoding.vibeplayer.feature.library.presentation.playlist

import com.rfcoding.vibeplayer.feature.library.presentation.playlistname.PlaylistNameAction

sealed interface PlaylistAction {
    data object OnSheetDismiss : PlaylistAction
    data object OnPlayFavouritesClick : PlaylistAction
    data class OnPlayPlaylistClick(val playlistId: Long) : PlaylistAction
    data class OnRenamePlaylistClick(val playlistId: Long) : PlaylistAction
    data class OnChangePlaylistCoverClick(val playlistId: Long) : PlaylistAction
    /** Opens the delete confirmation; [OnConfirmDeletePlaylistClick] is the one that deletes. */
    data class OnDeletePlaylistClick(val playlistId: Long) : PlaylistAction
    data class OnConfirmDeletePlaylistClick(val playlistId: Long) : PlaylistAction
    /** Forwards the name sheet's own actions so the screen keeps a single action funnel. */
    data class OnPlaylistNameAction(val action: PlaylistNameAction) : PlaylistAction
    data object OnCreatePlaylistClick : PlaylistAction
    data object OnFavouritesClick : PlaylistAction
    data object OnFavouritesMenuClick : PlaylistAction
    data class OnPlaylistClick(val playlistId: Long) : PlaylistAction
    data class OnPlaylistMenuClick(val playlistId: Long) : PlaylistAction
}