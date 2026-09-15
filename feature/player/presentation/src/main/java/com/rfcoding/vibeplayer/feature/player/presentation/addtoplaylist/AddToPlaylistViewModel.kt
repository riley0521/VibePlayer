package com.rfcoding.vibeplayer.feature.player.presentation.addtoplaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.domain.util.onSuccess
import com.rfcoding.vibeplayer.core.presentation.UiText
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
import com.rfcoding.vibeplayer.core.presentation.R as PresentationR

/**
 * Backs the add-to-playlist sheet for the song with [songId]. It lives in the sheet's own scope (see
 * `DialogSheetScopedViewModel`), so every opening starts fresh. Adding a song that is already in the
 * playlist leaves it as it is, because the database ignores the duplicate link.
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
                    playlists = playlists.map { playlist -> playlist.toPlaylistUi() },
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: AddToPlaylistAction) {
        when (action) {
            AddToPlaylistAction.OnFavouritesClick -> add(UiText.StringResource(PresentationR.string.favourites)) {
                songDataSource.setFavorite(songId, isFavorite = true)
            }
            is AddToPlaylistAction.OnPlaylistClick -> {
                val playlist = _state.value.playlists.find { it.id == action.playlistId } ?: return
                add(UiText.DynamicString(playlist.name)) {
                    playlistDataSource.addSongsToPlaylist(playlist.id, listOf(songId))
                }
            }
            // Handled by the Root.
            AddToPlaylistAction.OnCreatePlaylistClick -> Unit
        }
    }

    private fun add(playlistName: UiText, write: suspend () -> EmptyResult<DataError.Local>) {
        viewModelScope.launch {
            write()
                .onSuccess { eventChannel.send(AddToPlaylistEvent.AddedToPlaylist(playlistName)) }
                .onFailure { error -> eventChannel.send(AddToPlaylistEvent.Error(error.toUiText())) }
        }
    }
}
