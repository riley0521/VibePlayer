package com.rfcoding.vibeplayer.feature.library.presentation.library

sealed interface LibraryAction {
    data object OnScanClick : LibraryAction
    data object OnScanAgainClick : LibraryAction
    data object OnSearchClick : LibraryAction
    data class OnTabSelect(val tab: LibraryTab) : LibraryAction
    data object OnMiniPlayerClick : LibraryAction
    data object OnPlayPauseClick : LibraryAction
    data object OnSkipToPreviousClick : LibraryAction
    data object OnSkipNextClick : LibraryAction
    data class OnSeek(val fraction: Float) : LibraryAction
}
