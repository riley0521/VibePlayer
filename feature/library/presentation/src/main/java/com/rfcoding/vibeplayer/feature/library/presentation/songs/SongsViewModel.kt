package com.rfcoding.vibeplayer.feature.library.presentation.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.presentation.toSongUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Builds the play queue for the Songs tab; the player only ever plays the list it is handed.
 */
class SongsViewModel(
    songDataSource: SongLocalDataSource,
    private val musicPlayer: MusicPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(SongsState())
    val state = _state.asStateFlow()

    /** Kept as domain songs because [SongsState] has no file URIs, which the player needs. */
    private var songs: List<Song> = emptyList()

    init {
        songDataSource.songs
            .onEach { songs ->
                this.songs = songs
                _state.update { it.copy(songs = songs.map { song -> song.toSongUi() }) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SongsAction) {
        when (action) {
            SongsAction.OnPlayClick -> playQueue(songs)
            SongsAction.OnShuffleClick -> playQueue(songs.shuffled())
            is SongsAction.OnSongClick -> {
                val index = songs.indexOfFirst { it.id == action.songId }
                // Only the tapped song and the ones after it are queued.
                if (index != -1) playQueue(songs.drop(index))
            }
        }
    }

    private fun playQueue(queue: List<Song>) {
        if (queue.isEmpty()) return
        viewModelScope.launch { musicPlayer.play(queue) }
    }
}
