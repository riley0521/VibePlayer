package com.rfcoding.vibeplayer.feature.library.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.domain.util.onSuccess
import com.rfcoding.vibeplayer.core.presentation.toNowPlayingUi
import com.rfcoding.vibeplayer.core.presentation.toUiText
import com.rfcoding.vibeplayer.feature.library.domain.MusicLibraryRepository
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Scans on open: with an empty library the scan is shown (Scanning, then No music found if nothing
 * turned up); with songs already stored it runs silently behind the list.
 */
class LibraryViewModel(
    private val musicLibraryRepository: MusicLibraryRepository,
    private val songDataSource: SongLocalDataSource,
    private val musicPlayer: MusicPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<LibraryEvent>()
    val events = eventChannel.receiveAsFlow()

    /**
     * Whether a scan the user can see is running. Starts true so an empty library reads as
     * Scanning, not No music found, until the first scan has had its say.
     */
    private val isVisibleScanRunning = MutableStateFlow(true)

    init {
        combine(songDataSource.songs, isVisibleScanRunning) { songs, isScanning ->
            _state.update {
                it.copy(
                    status = when {
                        songs.isNotEmpty() -> LibraryStatus.Loaded
                        isScanning -> LibraryStatus.Scanning
                        else -> LibraryStatus.NoMusicFound
                    },
                )
            }
        }.launchIn(viewModelScope)

        // A scan's later batches sync after the scan UI is gone, so their failures surface here.
        musicLibraryRepository.incompleteScans
            .onEach { eventChannel.send(LibraryEvent.Error(it.toUiText())) }
            .launchIn(viewModelScope)

        musicPlayer.playbackState
            .onEach { playback -> _state.update { it.copy(nowPlaying = playback.toNowPlayingUi()) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            if (songDataSource.songs.first().isEmpty()) {
                scanVisibly()
            } else {
                isVisibleScanRunning.value = false
                // A background refresh; the list on screen is still valid if it fails.
                musicLibraryRepository.scanMusic(ScanFilters())
            }
        }
    }

    fun onAction(action: LibraryAction) {
        when (action) {
            LibraryAction.OnScanAgainClick -> {
                if (!isVisibleScanRunning.value) viewModelScope.launch { scanVisibly() }
            }
            is LibraryAction.OnTabSelect -> _state.update { it.copy(selectedTab = action.tab) }
            LibraryAction.OnPlayPauseClick -> viewModelScope.launch { musicPlayer.togglePlayPause() }
            LibraryAction.OnSkipToPreviousClick -> viewModelScope.launch { musicPlayer.skipToPrevious() }
            LibraryAction.OnSkipNextClick -> viewModelScope.launch { musicPlayer.skipToNext() }
            is LibraryAction.OnSeek -> seek(action.fraction)
            // Navigation is handled by the Root.
            else -> Unit
        }
    }

    private fun seek(fraction: Float) {
        val positionMillis = musicPlayer.playbackState.value.seekPositionFor(fraction) ?: return
        // Shown straight away, so the released seek bar doesn't jump back until the session catches up.
        _state.update { it.copy(nowPlaying = it.nowPlaying?.copy(positionMillis = positionMillis)) }
        viewModelScope.launch { musicPlayer.seekTo(positionMillis) }
    }

    private suspend fun scanVisibly() {
        isVisibleScanRunning.value = true
        musicLibraryRepository.scanMusic(ScanFilters())
            .onSuccess { songCount ->
                // Room re-queries after the sync commits, so wait for the songs to arrive before
                // dropping the flag; otherwise No music found flashes in between.
                if (songCount > 0) {
                    songDataSource.songs.first { it.isNotEmpty() }
                }
            }
            .onFailure { error -> eventChannel.send(LibraryEvent.Error(error.toUiText())) }

        delay(3.seconds)
        isVisibleScanRunning.value = false
    }
}
