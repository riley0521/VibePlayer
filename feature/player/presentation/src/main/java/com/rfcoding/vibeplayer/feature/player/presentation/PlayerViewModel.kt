package com.rfcoding.vibeplayer.feature.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.onFailure
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
 */
class PlayerViewModel(
    private val musicPlayer: MusicPlayer,
    private val songDataSource: SongLocalDataSource,
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
            // Navigation is handled by the Root. Seek and add to playlist come later.
            else -> Unit
        }
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
