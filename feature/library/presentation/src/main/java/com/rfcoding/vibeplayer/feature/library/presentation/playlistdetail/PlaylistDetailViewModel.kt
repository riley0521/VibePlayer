package com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.domain.util.onSuccess
import com.rfcoding.vibeplayer.core.presentation.toPlaylistUi
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
 * Backs the Playlist Page of one playlist, or of the virtual Favourites when [playlistId] is null, and
 * builds the queues started from it. In delete mode the ticked songs are removed from the playlist, or
 * unfavourited when this is Favourites.
 */
class PlaylistDetailViewModel(
    private val playlistId: Long?,
    private val playlistDataSource: PlaylistLocalDataSource,
    private val songDataSource: SongLocalDataSource,
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
            PlaylistDetailAction.OnEditClick -> {
                if (_state.value.canEditSongs) _state.update { it.copy(isDeleteMode = true) }
            }
            PlaylistDetailAction.OnExitDeleteModeClick -> exitDeleteMode()
            is PlaylistDetailAction.OnSongSelectedChange -> updateSelection { selected ->
                if (action.selected) selected + action.songId else selected - action.songId
            }
            is PlaylistDetailAction.OnSelectAllChange -> updateSelection {
                if (action.selected) songs.map { song -> song.id }.toSet() else emptySet()
            }
            PlaylistDetailAction.OnDeleteSelectedClick -> {
                if (_state.value.hasSelection) _state.update { it.copy(isRemoveSheetVisible = true) }
            }
            PlaylistDetailAction.OnDismissRemoveSheet -> _state.update { it.copy(isRemoveSheetVisible = false) }
            PlaylistDetailAction.OnConfirmRemoveClick -> removeSelectedSongs()
            // Navigation is handled by the Root.
            PlaylistDetailAction.OnBackClick, PlaylistDetailAction.OnAddSongsClick -> Unit
        }
    }

    private fun showSongs(songs: List<Song>) {
        this.songs = songs
        val songIds = songs.map { it.id }.toSet()
        _state.update {
            it.copy(
                songs = songs.map { song -> song.toSongUi() },
                isLoading = false,
                // Songs can vanish underneath delete mode (a rescan, the Player's heart), so drop
                // their ticks, and leave delete mode once there is nothing left to remove.
                selectedSongIds = it.selectedSongIds intersect songIds,
                isDeleteMode = it.isDeleteMode && songs.isNotEmpty(),
                isRemoveSheetVisible = it.isRemoveSheetVisible && songs.isNotEmpty(),
            )
        }
    }

    private fun updateSelection(transform: (Set<String>) -> Set<String>) {
        if (!_state.value.isDeleteMode) return
        _state.update { it.copy(selectedSongIds = transform(it.selectedSongIds)) }
    }

    private fun exitDeleteMode() {
        _state.update { it.copy(isDeleteMode = false, selectedSongIds = emptySet(), isRemoveSheetVisible = false) }
    }

    private fun removeSelectedSongs() {
        val current = _state.value
        if (current.isRemoving || !current.hasSelection) return

        // Playlist order, which keeps the write deterministic.
        val songIds = songs.filter { it.id in current.selectedSongIds }.map { it.id }
        _state.update { it.copy(isRemoving = true) }
        viewModelScope.launch {
            val result = if (playlistId == null) {
                songDataSource.setFavorites(songIds, isFavorite = false)
            } else {
                playlistDataSource.removeSongsFromPlaylist(playlistId, songIds)
            }
            result
                .onSuccess { exitDeleteMode() }
                .onFailure { error ->
                    _state.update { it.copy(isRemoveSheetVisible = false) }
                    eventChannel.send(PlaylistDetailEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isRemoving = false) }
        }
    }

    private fun playQueue(queue: List<Song>) {
        if (queue.isEmpty()) return
        // Playlist queues wrap around when the Player's artwork is swiped past an end.
        viewModelScope.launch { musicPlayer.play(queue, isPlaylist = true) }
    }
}
