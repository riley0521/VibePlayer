package com.rfcoding.vibeplayer.feature.library.presentation.library

sealed interface LibraryAction {
    data object OnScanClick : LibraryAction
    data object OnScanAgainClick : LibraryAction
}
