package com.rfcoding.vibeplayer.feature.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.domain.util.onSuccess
import com.rfcoding.vibeplayer.core.presentation.UiText
import com.rfcoding.vibeplayer.core.presentation.toSongUi
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
 * Mirrors the playback session, plus the current song's favourite flag from the database. Shuffle is
 * performed by the playback service, which the media notification uses too, so both always agree.
 *
 * It also decides which sheet is open. The add-to-playlist sheet does its own writes; only a playlist
 * created from it gets its song added here, because the shared create sheet knows nothing about songs.
 */
class PlayerViewModel(
    private val musicPlayer: MusicPlayer,
    private val songDataSource: SongLocalDataSource,
    private val playlistDataSource: PlaylistLocalDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<PlayerEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        combine(musicPlayer.playbackState, songDataSource.songs) { playback, songs ->
            val currentSong = playback.currentSong
            _state.update {
                it.copy(
                    song = currentSong?.toSongUi(),
                    isPlaying = playback.isPlaying,
                    positionMillis = playback.positionMillis,
                    // The queue's copy of the song can be stale; the database row is the truth.
                    isFavorite = songs.find { song -> song.id == currentSong?.id }?.isFavorite == true,
                    isShuffleOn = playback.isShuffleOn,
                    repeatMode = playback.repeatMode,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: PlayerAction) {
        when (action) {
            PlayerAction.OnPlayPauseClick -> viewModelScope.launch { musicPlayer.togglePlayPause() }
            PlayerAction.OnNextClick -> viewModelScope.launch { musicPlayer.skipToNext() }
            PlayerAction.OnPreviousClick -> viewModelScope.launch { musicPlayer.skipToPrevious() }
            PlayerAction.OnShuffleClick -> viewModelScope.launch { musicPlayer.toggleShuffle() }
            PlayerAction.OnRepeatClick -> viewModelScope.launch {
                musicPlayer.setRepeatMode(musicPlayer.playbackState.value.repeatMode.next())
            }
            PlayerAction.OnFavoriteClick -> toggleFavorite()
            is PlayerAction.OnSeek -> seek(action.fraction)
            PlayerAction.OnAddToPlaylistClick -> musicPlayer.playbackState.value.currentSong?.let { song ->
                openSheet(PlayerSheet.AddToPlaylist(song.id))
            }
            PlayerAction.OnCreatePlaylistClick -> _state.value.activeSheet?.let { sheet ->
                openSheet(PlayerSheet.CreatePlaylist(sheet.songId))
            }
            is PlayerAction.OnPlaylistCreated -> addToCreatedPlaylist(action.playlistId, action.name)
            PlayerAction.OnSheetDismiss -> closeSheet()
            // Handled by the Root.
            PlayerAction.OnBackClick -> Unit
        }
    }

    private fun openSheet(sheet: PlayerSheet) = _state.update { it.copy(activeSheet = sheet) }

    private fun closeSheet() = _state.update { it.copy(activeSheet = null) }

    private fun addToCreatedPlaylist(playlistId: Long, name: String) {
        val songId = (_state.value.activeSheet as? PlayerSheet.CreatePlaylist)?.songId ?: return
        closeSheet()
        viewModelScope.launch {
            playlistDataSource.addSongsToPlaylist(playlistId, listOf(songId))
                .onSuccess { eventChannel.send(PlayerEvent.AddedToPlaylist(UiText.DynamicString(name))) }
                .onFailure { error -> eventChannel.send(PlayerEvent.Error(error.toUiText())) }
        }
    }

    private fun seek(fraction: Float) {
        val positionMillis = musicPlayer.playbackState.value.seekPositionFor(fraction) ?: return
        // Shown straight away, so the released seek bar doesn't jump back until the session catches up.
        _state.update { it.copy(positionMillis = positionMillis) }
        viewModelScope.launch { musicPlayer.seekTo(positionMillis) }
    }

    private fun toggleFavorite() {
        val songId = musicPlayer.playbackState.value.currentSong?.id ?: return
        val isFavorite = state.value.isFavorite
        viewModelScope.launch {
            songDataSource.setFavorite(songId, !isFavorite)
                .onFailure { error -> eventChannel.send(PlayerEvent.Error(error.toUiText())) }
        }
    }

    /** Off → All → One → Off, the order the repeat button's icons follow. */
    private fun RepeatMode.next(): RepeatMode = when (this) {
        RepeatMode.Off -> RepeatMode.All
        RepeatMode.All -> RepeatMode.One
        RepeatMode.One -> RepeatMode.Off
    }
}
