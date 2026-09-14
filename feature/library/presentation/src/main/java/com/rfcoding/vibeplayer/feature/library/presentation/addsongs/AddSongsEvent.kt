package com.rfcoding.vibeplayer.feature.library.presentation.addsongs

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface AddSongsEvent {
    data object SongsAdded : AddSongsEvent
    data class Error(val message: UiText) : AddSongsEvent
}
