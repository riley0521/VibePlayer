package com.rfcoding.vibeplayer.feature.library.presentation.addsongs

sealed interface AddSongsAction {
    data object OnBackClick : AddSongsAction
    data class OnQueryChange(val query: String) : AddSongsAction
    data object OnClearQueryClick : AddSongsAction
    data class OnSelectAllChange(val selected: Boolean) : AddSongsAction
    data class OnSongSelectedChange(val songId: String, val selected: Boolean) : AddSongsAction
    data object OnOkClick : AddSongsAction
}
