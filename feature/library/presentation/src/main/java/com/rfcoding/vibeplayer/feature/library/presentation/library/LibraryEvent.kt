package com.rfcoding.vibeplayer.feature.library.presentation.library

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface LibraryEvent {
    data class Error(val message: UiText) : LibraryEvent
}
