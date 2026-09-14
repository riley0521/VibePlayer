package com.rfcoding.vibeplayer.feature.library.presentation.addsongs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.domain.util.onSuccess
import com.rfcoding.vibeplayer.core.presentation.toSongUi
import com.rfcoding.vibeplayer.core.presentation.toUiText
import com.rfcoding.vibeplayer.feature.library.domain.filterByQuery
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Picks songs for the playlist [playlistId]. The query and the ticked songs go into
 * [SavedStateHandle] so a half-made selection survives process death.
 */
class AddSongsViewModel(
    private val playlistId: Long,
    private val savedStateHandle: SavedStateHandle,
    songDataSource: SongLocalDataSource,
    private val playlistDataSource: PlaylistLocalDataSource,
) : ViewModel() {

    private val query = MutableStateFlow(savedStateHandle[KEY_QUERY] ?: "")

    private val _state = MutableStateFlow(
        AddSongsState(
            query = query.value,
            selectedSongIds = savedStateHandle.get<ArrayList<String>>(KEY_SELECTED_SONG_IDS)?.toSet().orEmpty(),
        )
    )
    val state = _state.asStateFlow()

    private val eventChannel = Channel<AddSongsEvent>()
    val events = eventChannel.receiveAsFlow()

    /** The whole library, unfiltered, so OK can keep songs ticked before the current query. */
    private var allSongs: List<Song> = emptyList()

    init {
        combine(songDataSource.songs, query) { songs, query ->
            allSongs = songs
            _state.update {
                it.copy(
                    query = query,
                    songs = songs.filterByQuery(query).map { song -> song.toSongUi() },
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: AddSongsAction) {
        when (action) {
            is AddSongsAction.OnQueryChange -> setQuery(action.query)
            AddSongsAction.OnClearQueryClick -> setQuery("")
            is AddSongsAction.OnSongSelectedChange -> updateSelection { selected ->
                if (action.selected) selected + action.songId else selected - action.songId
            }
            // Only the songs on screen are affected; ticks hidden by the query stay as they are.
            is AddSongsAction.OnSelectAllChange -> updateSelection { selected ->
                val visibleIds = _state.value.songs.map { it.id }
                if (action.selected) selected + visibleIds else selected - visibleIds.toSet()
            }
            AddSongsAction.OnOkClick -> addSelectedSongs()
            // Handled by the Root.
            AddSongsAction.OnBackClick -> Unit
        }
    }

    private fun setQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
        this.query.value = query
    }

    private fun updateSelection(transform: (Set<String>) -> Set<String>) {
        val selected = transform(_state.value.selectedSongIds)
        savedStateHandle[KEY_SELECTED_SONG_IDS] = ArrayList(selected)
        _state.update { it.copy(selectedSongIds = selected) }
    }

    private fun addSelectedSongs() {
        val current = _state.value
        if (current.isSaving || !current.hasSelection) return

        // Library order, and only songs that still exist after a rescan.
        val songIds = allSongs.filter { it.id in current.selectedSongIds }.map { it.id }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            playlistDataSource.addSongsToPlaylist(playlistId, songIds)
                .onSuccess { eventChannel.send(AddSongsEvent.SongsAdded) }
                .onFailure { error -> eventChannel.send(AddSongsEvent.Error(error.toUiText())) }
            _state.update { it.copy(isSaving = false) }
        }
    }

    private companion object {
        const val KEY_QUERY = "query"
        const val KEY_SELECTED_SONG_IDS = "selectedSongIds"
    }
}
