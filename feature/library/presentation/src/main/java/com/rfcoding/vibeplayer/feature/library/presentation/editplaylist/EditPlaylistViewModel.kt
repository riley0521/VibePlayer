package com.rfcoding.vibeplayer.feature.library.presentation.editplaylist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.domain.util.onSuccess
import com.rfcoding.vibeplayer.core.presentation.SongUi
import com.rfcoding.vibeplayer.core.presentation.toSongUi
import com.rfcoding.vibeplayer.core.presentation.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backs the Edit playlist screen, where songs are reordered and removed. Nothing reaches the database
 * until Save, so the working order lives here and in [SavedStateHandle], which keeps a half-finished
 * edit across process death.
 *
 * The playlist is only read once. Later emissions are ignored, because a rescan or a heart tapped in
 * the player would otherwise throw away the edit in progress; the save then writes only the songs
 * that are still there.
 */
class EditPlaylistViewModel(
    private val playlistId: Long,
    private val savedStateHandle: SavedStateHandle,
    private val playlistDataSource: PlaylistLocalDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(EditPlaylistState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<EditPlaylistEvent>()
    val events = eventChannel.receiveAsFlow()

    /** The order the screen opened with, which [EditPlaylistState.hasChanges] is measured against. */
    private var originalSongIds: List<String> = emptyList()

    init {
        playlistDataSource.observePlaylist(playlistId)
            .onEach { playlist ->
                if (playlist == null) {
                    eventChannel.send(EditPlaylistEvent.NavigateBack)
                    return@onEach
                }
                if (!_state.value.isLoading) return@onEach
                seed(playlist.songs.map { it.toSongUi() })
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: EditPlaylistAction) {
        when (action) {
            EditPlaylistAction.OnBackClick -> confirmLeaving()
            EditPlaylistAction.OnSaveClick -> save()
            is EditPlaylistAction.OnRemoveSongClick -> updateSongs { songs ->
                songs.filterNot { it.id == action.songId }
            }
            is EditPlaylistAction.OnMoveSong -> moveSong(action.from, action.to)
            EditPlaylistAction.OnDismissDiscardSheet -> {
                _state.update { it.copy(isDiscardSheetVisible = false) }
            }
            EditPlaylistAction.OnConfirmDiscardClick -> viewModelScope.launch {
                _state.update { it.copy(isDiscardSheetVisible = false) }
                eventChannel.send(EditPlaylistEvent.NavigateBack)
            }
        }
    }

    private fun seed(songs: List<SongUi>) {
        // A saved order only applies to the songs the playlist still holds.
        val savedOrder = savedStateHandle.get<ArrayList<String>>(KEY_SONG_IDS)
        val working = if (savedOrder == null) {
            songs
        } else {
            val songsById = songs.associateBy { it.id }
            savedOrder.mapNotNull { songsById[it] }
        }

        originalSongIds = songs.map { it.id }
        _state.update {
            it.copy(
                songs = working,
                isLoading = false,
                hasChanges = working.map { song -> song.id } != originalSongIds,
            )
        }
    }

    private fun moveSong(from: Int, to: Int) = updateSongs { songs ->
        if (from !in songs.indices || to !in songs.indices) {
            songs
        } else {
            songs.toMutableList().apply { add(to, removeAt(from)) }
        }
    }

    private fun updateSongs(transform: (List<SongUi>) -> List<SongUi>) {
        if (_state.value.isSaving) return
        val songs = transform(_state.value.songs)
        val songIds = songs.map { it.id }
        savedStateHandle[KEY_SONG_IDS] = ArrayList(songIds)
        _state.update { it.copy(songs = songs, hasChanges = songIds != originalSongIds) }
    }

    private fun confirmLeaving() {
        if (_state.value.hasChanges) {
            _state.update { it.copy(isDiscardSheetVisible = true) }
        } else {
            viewModelScope.launch { eventChannel.send(EditPlaylistEvent.NavigateBack) }
        }
    }

    private fun save() {
        if (!_state.value.canSave) return

        val songIds = _state.value.songs.map { it.id }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            playlistDataSource.setPlaylistSongs(playlistId, songIds)
                .onSuccess { eventChannel.send(EditPlaylistEvent.NavigateBack) }
                .onFailure { error -> eventChannel.send(EditPlaylistEvent.Error(error.toUiText())) }
            _state.update { it.copy(isSaving = false) }
        }
    }

    private companion object {
        const val KEY_SONG_IDS = "songIds"
    }
}
