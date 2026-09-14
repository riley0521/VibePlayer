package com.rfcoding.vibeplayer.feature.library.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.domain.util.onSuccess
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
    private val songDataSource: SongLocalDataSource
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
            // Navigation is handled by the Root; playback and playlist actions land with their features.
            else -> Unit
        }
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
