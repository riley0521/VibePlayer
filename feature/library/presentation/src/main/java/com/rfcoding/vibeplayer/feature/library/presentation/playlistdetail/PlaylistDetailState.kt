package com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail

import com.rfcoding.vibeplayer.core.presentation.SongUi

data class PlaylistDetailState(
    /** The virtual Favourites has no row, so its name comes from a string resource instead of [name]. */
    val isFavourites: Boolean = false,
    val name: String = "",
    /** The playlist's cover, or its first song's artwork when it has none. */
    val imageUri: String? = null,
    val songs: List<SongUi> = emptyList(),
    /** True until the first emission, so an empty playlist doesn't flash while it loads. */
    val isLoading: Boolean = true,
) {
    /** Favourites fills up from the Player's heart, so it offers no Add Songs button. */
    val canAddSongs: Boolean get() = !isFavourites
}
