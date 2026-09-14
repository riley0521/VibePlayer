package com.rfcoding.vibeplayer.feature.library.presentation.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.playlist.Playlist
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.presentation.toPlaylistUi
import com.rfcoding.vibeplayer.core.presentation.toUiText
import com.rfcoding.vibeplayer.feature.library.presentation.playlistname.PlaylistNameMode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val playlistDataSource: PlaylistLocalDataSource,
    songDataSource: SongLocalDataSource,
    private val musicPlayer: MusicPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<PlaylistEvent>()
    val events = eventChannel.receiveAsFlow()

    /** Kept as domain models because the UI models have no file URIs, which the player needs. */
    private var playlists: List<Playlist> = emptyList()
    private var favourites: List<Song> = emptyList()

    init {
        combine(
            playlistDataSource.observePlaylists(),
            songDataSource.observeFavoriteSongs(),
        ) { playlists, favourites ->
            this.playlists = playlists
            this.favourites = favourites
            _state.update {
                it.copy(
                    playlists = playlists.map { playlist -> playlist.toPlaylistUi() },
                    favouriteSongCount = favourites.size,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: PlaylistAction) {
        when (action) {
            PlaylistAction.OnCreatePlaylistClick -> openSheet(PlaylistSheet.PlaylistName(PlaylistNameMode.Create))
            PlaylistAction.OnFavouritesMenuClick -> openSheet(
                PlaylistSheet.PlaylistActions(playlist = null, favouriteSongCount = favourites.size)
            )
            is PlaylistAction.OnPlaylistMenuClick -> findPlaylistUi(action.playlistId)?.let {
                openSheet(PlaylistSheet.PlaylistActions(playlist = it))
            }
            PlaylistAction.OnSheetDismiss -> closeSheet()
            PlaylistAction.OnPlayFavouritesClick -> {
                closeSheet()
                playQueue(favourites)
            }
            is PlaylistAction.OnPlayPlaylistClick -> {
                closeSheet()
                playQueue(playlists.find { it.id == action.playlistId }?.songs.orEmpty())
            }
            is PlaylistAction.OnRenamePlaylistClick -> playlists.find { it.id == action.playlistId }?.let {
                openSheet(PlaylistSheet.PlaylistName(PlaylistNameMode.Rename(it.id, it.name)))
            }
            is PlaylistAction.OnDeletePlaylistClick -> findPlaylistUi(action.playlistId)?.let {
                openSheet(PlaylistSheet.DeletePlaylist(it))
            }
            is PlaylistAction.OnConfirmDeletePlaylistClick -> {
                closeSheet()
                runDatabaseUpdate { playlistDataSource.deletePlaylist(action.playlistId) }
            }
            is PlaylistAction.OnChangePlaylistCoverClick -> {
                closeSheet()
                // Saved so the result can still be matched to its playlist after process death.
                savedStateHandle[KEY_COVER_PLAYLIST_ID] = action.playlistId
                viewModelScope.launch { eventChannel.send(PlaylistEvent.LaunchCoverPicker) }
            }
            is PlaylistAction.OnCoverPicked -> {
                val playlistId = savedStateHandle.remove<Long>(KEY_COVER_PLAYLIST_ID)
                if (playlistId != null && action.uri != null) {
                    runDatabaseUpdate { playlistDataSource.setPlaylistCover(playlistId, action.uri) }
                }
            }
            // No playlist detail screen yet.
            PlaylistAction.OnFavouritesClick, is PlaylistAction.OnPlaylistClick -> Unit
        }
    }

    private fun openSheet(sheet: PlaylistSheet) = _state.update { it.copy(activeSheet = sheet) }

    private fun closeSheet() = _state.update { it.copy(activeSheet = null) }

    private fun findPlaylistUi(playlistId: Long) = _state.value.playlists.find { it.id == playlistId }

    private fun playQueue(queue: List<Song>) {
        if (queue.isEmpty()) return
        viewModelScope.launch { musicPlayer.play(queue) }
    }

    private fun runDatabaseUpdate(update: suspend () -> EmptyResult<DataError.Local>) {
        viewModelScope.launch {
            update().onFailure { error -> eventChannel.send(PlaylistEvent.Error(error.toUiText())) }
        }
    }

    private companion object {
        const val KEY_COVER_PLAYLIST_ID = "coverPlaylistId"
    }
}
