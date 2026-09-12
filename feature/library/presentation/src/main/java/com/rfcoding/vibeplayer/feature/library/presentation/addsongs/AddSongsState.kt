package com.rfcoding.vibeplayer.feature.library.presentation.addsongs

import androidx.compose.runtime.Stable
import com.rfcoding.vibeplayer.core.presentation.SongUi

@Stable
data class AddSongsState(
    val query: String = "",
    /** Already filtered for [query]; an empty query keeps every song, as on the Search screen. */
    val songs: List<SongUi> = emptyList(),
    val selectedSongIds: Set<String> = emptySet(),
) {
    val selectedCount: Int get() = selectedSongIds.size

    /** Figma only shows the OK button once something is ticked. */
    val hasSelection: Boolean get() = selectedSongIds.isNotEmpty()

    /**
     * Select All reflects the songs currently on screen, so a search that matches nothing leaves it
     * unchecked and songs ticked before the query was typed don't tick it either.
     */
    val isAllSelected: Boolean
        get() = songs.isNotEmpty() && songs.all { it.id in selectedSongIds }
}
