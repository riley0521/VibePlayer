package com.rfcoding.vibeplayer.feature.library.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.presentation.toSongUi
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
 * Filters the library as the user types and plays the tapped result. The query goes into
 * [SavedStateHandle] so it survives process death.
 */
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    songDataSource: SongLocalDataSource,
    private val musicPlayer: MusicPlayer,
) : ViewModel() {

    private val query = MutableStateFlow(savedStateHandle[KEY_QUERY] ?: "")

    private val _state = MutableStateFlow(SearchState(query = query.value))
    val state = _state.asStateFlow()

    private val eventChannel = Channel<SearchEvent>()
    val events = eventChannel.receiveAsFlow()

    /** The results on screen, kept as domain songs because [SearchState] has no file URIs. */
    private var results: List<Song> = emptyList()

    init {
        combine(songDataSource.songs, query) { songs, query ->
            results = songs.filterByQuery(query)
            _state.update { it.copy(query = query, songs = results.map { song -> song.toSongUi() }) }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.OnQueryChange -> setQuery(action.query)
            SearchAction.OnClearClick -> setQuery("")
            is SearchAction.OnSongClick -> playFrom(action.songId)
            // Handled by the Root.
            SearchAction.OnCancelClick -> Unit
        }
    }

    private fun setQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
        this.query.value = query
    }

    private fun playFrom(songId: String) {
        val index = results.indexOfFirst { it.id == songId }
        if (index == -1) return
        // Only the tapped result and the results after it are queued, as on the Songs tab.
        val queue = results.drop(index)
        viewModelScope.launch {
            musicPlayer.play(queue)
            eventChannel.send(SearchEvent.NavigateToPlayer)
        }
    }

    private companion object {
        const val KEY_QUERY = "query"
    }
}
