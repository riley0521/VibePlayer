package com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.presentation.toPlaylistUi
import com.rfcoding.vibeplayer.core.presentation.toSongUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backs the Playlist Page of one playlist, or of the virtual Favourites when [playlistId] is null, and
 * builds the queues started from it.
 */
class PlaylistDetailViewModel(
    private val playlistId: Long?,
    playlistDataSource: PlaylistLocalDataSource,
    songDataSource: SongLocalDataSource,
    private val musicPlayer: MusicPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistDetailState(isFavourites = playlistId == null))
    val state = _state.asStateFlow()

    private val eventChannel = Channel<PlaylistDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    /** Kept as domain songs because [PlaylistDetailState] has no file URIs, which the player needs. */
    private var songs: List<Song> = emptyList()

    init {
        if (playlistId == null) {
            songDataSource.observeFavoriteSongs()
                .onEach { favourites -> showSongs(favourites) }
                .launchIn(viewModelScope)
        } else {
            playlistDataSource.observePlaylist(playlistId)
                .onEach { playlist ->
                    if (playlist == null) {
                        eventChannel.send(PlaylistDetailEvent.NavigateBack)
                        return@onEach
                    }
                    val playlistUi = playlist.toPlaylistUi()
                    _state.update { it.copy(name = playlistUi.name, imageUri = playlistUi.imageUri) }
                    showSongs(playlist.songs)
                }
                .launchIn(viewModelScope)
        }
    }

    fun onAction(action: PlaylistDetailAction) {
        when (action) {
            PlaylistDetailAction.OnPlayClick -> playQueue(songs)
            PlaylistDetailAction.OnShuffleClick -> playQueue(songs.shuffled())
            is PlaylistDetailAction.OnSongClick -> {
                val index = songs.indexOfFirst { it.id == action.songId }
                // Only the tapped song and the ones after it are queued.
                if (index != -1) playQueue(songs.drop(index))
            }
            // Navigation is handled by the Root.
            PlaylistDetailAction.OnBackClick, PlaylistDetailAction.OnAddSongsClick -> Unit
        }
    }

    private fun showSongs(songs: List<Song>) {
        this.songs = songs
        _state.update { it.copy(songs = songs.map { song -> song.toSongUi() }, isLoading = false) }
    }

    private fun playQueue(queue: List<Song>) {
        if (queue.isEmpty()) return
        viewModelScope.launch { musicPlayer.play(queue) }
    }
}
