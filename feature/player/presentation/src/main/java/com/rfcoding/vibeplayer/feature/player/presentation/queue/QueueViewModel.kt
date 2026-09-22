package com.rfcoding.vibeplayer.feature.player.presentation.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.player.PlaybackState
import com.rfcoding.vibeplayer.core.domain.player.next
import com.rfcoding.vibeplayer.core.presentation.toSongUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backs the Queue sheet, which lists the current song and the upcoming ones. It lives in the sheet's
 * own scope (see `DialogSheetScopedViewModel`), so every opening starts from the player's queue.
 *
 * Drags and swipes change [QueueState.upcomingSongs] straight away and only then reach the player.
 * The list is rebuilt from the player only when its queue actually changes, so neither a drag in
 * progress nor the position updates between a drop and the player's answer undo what the user sees.
 */
class QueueViewModel(
    private val musicPlayer: MusicPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(QueueState())
    val state = _state.asStateFlow()

    /** The queue [QueueState.upcomingSongs] was last taken from, or last sent a change against. */
    private var syncedQueue: QueueKey? = null

    /** The song being dragged and where it started, or null while no drag runs. */
    private var drag: Drag? = null

    init {
        musicPlayer.playbackState
            .onEach { playback ->
                _state.update {
                    it.copy(
                        currentSong = playback.currentSong?.toSongUi(),
                        isPlaying = playback.isPlaying,
                        isShuffleOn = playback.isShuffleOn,
                        repeatMode = playback.repeatMode,
                    )
                }
                if (drag == null && playback.queueKey() != syncedQueue) syncFrom(playback)
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: QueueAction) {
        when (action) {
            QueueAction.OnPlayPauseClick -> viewModelScope.launch { musicPlayer.togglePlayPause() }
            is QueueAction.OnUpcomingSongClick -> {
                val index = musicPlayer.playbackState.value.upcomingIndexOf(action.songId) ?: return
                viewModelScope.launch { musicPlayer.skipTo(index) }
            }
            is QueueAction.OnMoveSong -> moveSong(action.from, action.to)
            QueueAction.OnDragStopped -> finishDrag()
            is QueueAction.OnSongSwiped -> removeSong(action.songId)
            QueueAction.OnShuffleClick -> viewModelScope.launch { musicPlayer.toggleShuffle() }
            QueueAction.OnRepeatClick -> viewModelScope.launch {
                musicPlayer.setRepeatMode(musicPlayer.playbackState.value.repeatMode.next())
            }
            QueueAction.OnTimerClick -> _state.update { it.copy(isSleepTimerSheetVisible = true) }
            is QueueAction.OnSleepTimerSelect -> {
                _state.update { it.copy(isSleepTimerSheetVisible = false) }
                viewModelScope.launch { musicPlayer.setSleepTimer(action.option.timer) }
            }
            QueueAction.OnSleepTimerSheetDismiss -> _state.update { it.copy(isSleepTimerSheetVisible = false) }
        }
    }

    private fun moveSong(from: Int, to: Int) {
        val songs = _state.value.upcomingSongs
        if (from !in songs.indices || to !in songs.indices) return
        if (drag == null) drag = Drag(songId = songs[from].id, startIndex = from)
        _state.update { it.copy(upcomingSongs = songs.toMutableList().apply { add(to, removeAt(from)) }) }
    }

    private fun finishDrag() {
        val drag = drag ?: return
        this.drag = null
        val playback = musicPlayer.playbackState.value
        val endIndex = _state.value.upcomingSongs.indexOfFirst { it.id == drag.songId }
        val from = playback.upcomingIndexOf(drag.songId)
        if (endIndex == drag.startIndex || endIndex == -1 || from == null) {
            // Nothing to send, but the queue may have changed while the drag held the list.
            syncFrom(playback)
            return
        }
        val to = (playback.currentIndex + 1 + endIndex).coerceAtMost(playback.queue.lastIndex)
        syncedQueue = playback.queueKey()
        viewModelScope.launch { musicPlayer.moveQueueItem(from, to) }
    }

    private fun removeSong(songId: String) {
        val playback = musicPlayer.playbackState.value
        val index = playback.upcomingIndexOf(songId) ?: return
        _state.update { state -> state.copy(upcomingSongs = state.upcomingSongs.filterNot { it.id == songId }) }
        syncedQueue = playback.queueKey()
        viewModelScope.launch { musicPlayer.removeQueueItem(index) }
    }

    private fun syncFrom(playback: PlaybackState) {
        syncedQueue = playback.queueKey()
        _state.update { state ->
            state.copy(upcomingSongs = playback.queue.drop(playback.currentIndex + 1).map { it.toSongUi() })
        }
    }

    /** The song's index in [PlaybackState.queue], looking only after the current song. */
    private fun PlaybackState.upcomingIndexOf(songId: String): Int? {
        if (currentIndex < 0) return null
        return (currentIndex + 1..queue.lastIndex).firstOrNull { queue[it].id == songId }
    }

    private fun PlaybackState.queueKey() = QueueKey(queue.map { it.id }, currentIndex)

    private data class QueueKey(val songIds: List<String>, val currentIndex: Int)

    private data class Drag(val songId: String, val startIndex: Int)
}
