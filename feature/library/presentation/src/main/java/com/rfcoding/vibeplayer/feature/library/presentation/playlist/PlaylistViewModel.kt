package com.rfcoding.vibeplayer.feature.library.presentation.playlist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaylistViewModel: ViewModel() {

    private val _state = MutableStateFlow(PlaylistState())
    val state = _state.asStateFlow()

    fun onAction(action: PlaylistAction) {
        // TODO
    }
}