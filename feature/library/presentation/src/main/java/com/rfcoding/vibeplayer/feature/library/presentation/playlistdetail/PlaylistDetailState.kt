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
    /** Entered with the pen icon: the songs become selectable so they can be removed. */
    val isDeleteMode: Boolean = false,
    val selectedSongIds: Set<String> = emptySet(),
    val isRemoveSheetVisible: Boolean = false,
    val isRemoving: Boolean = false,
) {
    /** Favourites fills up from the Player's heart, so it offers no Add Songs button. */
    val canAddSongs: Boolean get() = !isFavourites

    /** The pen icon only shows when there is something to remove. */
    val canEditSongs: Boolean get() = !isLoading && songs.isNotEmpty()

    val selectedCount: Int get() = selectedSongIds.size

    val hasSelection: Boolean get() = selectedSongIds.isNotEmpty()

    val isAllSelected: Boolean
        get() = songs.isNotEmpty() && songs.all { it.id in selectedSongIds }
}
