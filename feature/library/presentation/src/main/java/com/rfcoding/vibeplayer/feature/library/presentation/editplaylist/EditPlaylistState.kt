package com.rfcoding.vibeplayer.feature.library.presentation.editplaylist

import androidx.compose.runtime.Stable
import com.rfcoding.vibeplayer.core.presentation.SongUi

@Stable
data class EditPlaylistState(
    /** The working order, which only Save writes to the database. */
    val songs: List<SongUi> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    /** True once the working order differs from the one the screen opened with. */
    val hasChanges: Boolean = false,
    val isDiscardSheetVisible: Boolean = false,
) {
    val canSave: Boolean get() = hasChanges && !isSaving
}
