package com.rfcoding.vibeplayer.feature.library.presentation.playlist

sealed interface PlaylistAction {
    data object OnSheetDismiss : PlaylistAction
    data object OnPlayFavouritesClick : PlaylistAction
    data class OnPlayPlaylistClick(val playlistId: Long) : PlaylistAction
    data class OnRenamePlaylistClick(val playlistId: Long) : PlaylistAction
    /** Starts the photo picker; its result comes back as [OnCoverPicked]. */
    data class OnChangePlaylistCoverClick(val playlistId: Long) : PlaylistAction
    /** A null [uri] means the user closed the picker without choosing an image. */
    data class OnCoverPicked(val uri: String?) : PlaylistAction
    /** Opens the delete confirmation; [OnConfirmDeletePlaylistClick] is the one that deletes. */
    data class OnDeletePlaylistClick(val playlistId: Long) : PlaylistAction
    data class OnConfirmDeletePlaylistClick(val playlistId: Long) : PlaylistAction
    data object OnCreatePlaylistClick : PlaylistAction
    data object OnFavouritesClick : PlaylistAction
    data object OnFavouritesMenuClick : PlaylistAction
    data class OnPlaylistClick(val playlistId: Long) : PlaylistAction
    data class OnPlaylistMenuClick(val playlistId: Long) : PlaylistAction
}
