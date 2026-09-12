package com.rfcoding.vibeplayer.feature.library.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.domain.util.onSuccess
import com.rfcoding.vibeplayer.core.presentation.toUiText
import com.rfcoding.vibeplayer.feature.library.domain.MusicLibraryRepository
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScanMusicViewModel(
    private val musicLibraryRepository: MusicLibraryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ScanMusicState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<ScanMusicEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: ScanMusicAction) {
        when (action) {
            is ScanMusicAction.OnMinDurationSelect -> _state.update { it.copy(minDuration = action.minDuration) }
            is ScanMusicAction.OnMinSizeSelect -> _state.update { it.copy(minSize = action.minSize) }
            ScanMusicAction.OnScanClick -> scan()
            // Handled by the Root.
            ScanMusicAction.OnBackClick -> Unit
        }
    }

    private fun scan() {
        if (_state.value.isScanning) return
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true) }
            val filters = with(_state.value) { ScanFilters(minDuration = minDuration, minSize = minSize) }
            musicLibraryRepository.scanMusic(filters)
                .onSuccess { eventChannel.send(ScanMusicEvent.NavigateBack) }
                .onFailure { error ->
                    _state.update { it.copy(isScanning = false) }
                    eventChannel.send(ScanMusicEvent.Error(error.toUiText()))
                }
        }
    }
}
