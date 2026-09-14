package com.rfcoding.vibeplayer.feature.library.presentation.playlistname

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.domain.util.onSuccess
import com.rfcoding.vibeplayer.core.presentation.toUiText
import com.rfcoding.vibeplayer.feature.library.domain.PlaylistNameValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backs the create/rename sheet. It lives in the sheet's own scope (see `DialogSheetScopedViewModel`),
 * so every opening starts fresh while the typed name survives rotation. The name also goes into
 * [SavedStateHandle], but it only comes back after process death once the open sheet itself is
 * restored, which `PlaylistState.activeSheet` doesn't do yet.
 */
class PlaylistNameViewModel(
    private val mode: PlaylistNameMode,
    private val savedStateHandle: SavedStateHandle,
    private val playlistDataSource: PlaylistLocalDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(
        PlaylistNameState(
            mode = mode,
            name = savedStateHandle[KEY_NAME] ?: (mode as? PlaylistNameMode.Rename)?.currentName.orEmpty(),
        )
    )
    val state = _state.asStateFlow()

    private val eventChannel = Channel<PlaylistNameEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: PlaylistNameAction) {
        when (action) {
            is PlaylistNameAction.OnNameChange -> {
                savedStateHandle[KEY_NAME] = action.name
                _state.update { it.copy(name = action.name) }
            }
            PlaylistNameAction.OnConfirmClick -> confirm()
            // Handled by the Root.
            PlaylistNameAction.OnCancelClick -> Unit
        }
    }

    private fun confirm() {
        if (_state.value.isSaving) return
        val name = when (val validation = PlaylistNameValidator.validate(_state.value.name)) {
            is Result.Error -> {
                viewModelScope.launch { eventChannel.send(PlaylistNameEvent.Error(validation.error.toUiText())) }
                return
            }
            is Result.Success -> validation.data
        }

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (mode) {
                PlaylistNameMode.Create -> playlistDataSource.createPlaylist(name)
                    .onSuccess { id -> eventChannel.send(PlaylistNameEvent.PlaylistCreated(id)) }
                    .onFailure { error -> eventChannel.send(PlaylistNameEvent.Error(error.toUiText())) }
                is PlaylistNameMode.Rename -> playlistDataSource.renamePlaylist(mode.playlistId, name)
                    .onSuccess { eventChannel.send(PlaylistNameEvent.PlaylistRenamed) }
                    .onFailure { error -> eventChannel.send(PlaylistNameEvent.Error(error.toUiText())) }
            }
            _state.update { it.copy(isSaving = false) }
        }
    }

    private companion object {
        const val KEY_NAME = "name"
    }
}
