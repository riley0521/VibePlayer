package com.rfcoding.vibeplayer.feature.library.presentation.search

sealed interface SearchAction {
    data class OnQueryChange(val query: String) : SearchAction
    data object OnClearClick : SearchAction
    data object OnCancelClick : SearchAction
    data class OnSongClick(val songId: String) : SearchAction
}
