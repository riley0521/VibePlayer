package com.rfcoding.vibeplayer.feature.player.presentation.addtoplaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.presentation.toPlaylistUi
import com.rfcoding.vibeplayer.core.presentation.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backs the add-to-playlist sheet for the song with [songId]. It lives in the sheet's own scope (see
 * `DialogSheetScopedViewModel`), so every opening starts fresh. Tapping a row toggles the song in that
 * playlist (or its favourite flag), and the sheet stays open: the database flows update the row's icon.
 */
class AddToPlaylistViewModel(
    private val songId: String,
    private val playlistDataSource: PlaylistLocalDataSource,
    private val songDataSource: SongLocalDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(AddToPlaylistState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<AddToPlaylistEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        combine(
            playlistDataSource.observePlaylists(),
            songDataSource.observeFavoriteSongs(),
        ) { playlists, favourites ->
            _state.update {
                it.copy(
                    favouriteSongCount = favourites.size,
                    isFavourite = favourites.any { song -> song.id == songId },
                    playlists = playlists.map { playlist -> playlist.toPlaylistUi() },
                    playlistIdsWithSong = playlists
                        .filter { playlist -> playlist.songs.any { song -> song.id == songId } }
                        .map { playlist -> playlist.id }
                        .toSet(),
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: AddToPlaylistAction) {
        when (action) {
            AddToPlaylistAction.OnFavouritesClick -> {
                val isFavourite = _state.value.isFavourite
                write { songDataSource.setFavorite(songId, isFavorite = !isFavourite) }
            }
            is AddToPlaylistAction.OnPlaylistClick -> {
                val state = _state.value
                val playlist = state.playlists.find { it.id == action.playlistId } ?: return
                if (playlist.id in state.playlistIdsWithSong) {
                    write { playlistDataSource.removeSongsFromPlaylist(playlist.id, listOf(songId)) }
                } else {
                    write { playlistDataSource.addSongsToPlaylist(playlist.id, listOf(songId)) }
                }
            }
            // Handled by the Root.
            AddToPlaylistAction.OnCreatePlaylistClick -> Unit
        }
    }

    private fun write(block: suspend () -> EmptyResult<DataError.Local>) {
        viewModelScope.launch {
            block().onFailure { error -> eventChannel.send(AddToPlaylistEvent.Error(error.toUiText())) }
        }
    }
}
