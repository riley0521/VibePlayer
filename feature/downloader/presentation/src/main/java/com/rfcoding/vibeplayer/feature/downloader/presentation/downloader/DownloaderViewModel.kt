package com.rfcoding.vibeplayer.feature.downloader.presentation.downloader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.vibeplayer.core.designsystem.components.SongDownloadState
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.domain.util.onSuccess
import com.rfcoding.vibeplayer.core.presentation.toNowPlayingUi
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadQueue
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadStatus
import com.rfcoding.vibeplayer.feature.downloader.domain.LibraryKey
import com.rfcoding.vibeplayer.feature.downloader.domain.MediaLinkResolver
import com.rfcoding.vibeplayer.feature.downloader.domain.RemoteTrack
import com.rfcoding.vibeplayer.feature.downloader.domain.isIn
import com.rfcoding.vibeplayer.feature.downloader.domain.isYoutubeLink
import com.rfcoding.vibeplayer.feature.downloader.domain.toLibraryKeys
import com.rfcoding.vibeplayer.feature.downloader.domain.tracksToDownload
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Looks up a pasted link and shows its songs, each marked as in the library (matched on title and
 * artist), queued, downloading or downloadable. The link goes into [SavedStateHandle] so it
 * survives process death; the results are looked up again from it.
 */
class DownloaderViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val linkResolver: MediaLinkResolver,
    private val downloadQueue: DownloadQueue,
    songDataSource: SongLocalDataSource,
    private val musicPlayer: MusicPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(DownloaderState(url = savedStateHandle[KEY_URL] ?: ""))
    val state = _state.asStateFlow()

    private val eventChannel = Channel<DownloaderEvent>()
    val events = eventChannel.receiveAsFlow()

    private val results = MutableStateFlow<List<RemoteTrack>>(emptyList())
    private var resolveJob: Job? = null

    /** The latest inputs of the list, which Download all also needs. */
    private var libraryKeys: Set<LibraryKey> = emptySet()
    private var statuses: Map<String, DownloadStatus> = emptyMap()

    /** Null until the first statuses arrive, so failures from before this screen aren't reported. */
    private var previousStatuses: Map<String, DownloadStatus>? = null

    init {
        combine(
            results,
            songDataSource.songs.map { it.toLibraryKeys() },
            downloadQueue.statuses,
        ) { tracks, libraryKeys, statuses ->
            this.libraryKeys = libraryKeys
            this.statuses = statuses
            reportNewFailures(tracks, statuses)
            _state.update {
                it.copy(
                    tracks = tracks.map { track -> track.toUi(libraryKeys, statuses) },
                    canDownloadAll = tracks.size > 1 && tracksToDownload(tracks, libraryKeys, statuses).isNotEmpty(),
                )
            }
        }.launchIn(viewModelScope)

        musicPlayer.playbackState
            .onEach { playback -> _state.update { it.copy(nowPlaying = playback.toNowPlayingUi()) } }
            .launchIn(viewModelScope)

        if (isYoutubeLink(_state.value.url)) resolve()
    }

    fun onAction(action: DownloaderAction) {
        when (action) {
            is DownloaderAction.OnUrlChange -> setUrl(action.url)
            DownloaderAction.OnClearClick -> setUrl("")
            DownloaderAction.OnFindClick -> resolve()
            is DownloaderAction.OnPaste -> {
                setUrl(action.text.trim())
                resolve()
            }
            is DownloaderAction.OnDownloadClick -> download(action.videoId)
            DownloaderAction.OnDownloadAllClick -> downloadAll()
            DownloaderAction.OnPlayPauseClick -> viewModelScope.launch { musicPlayer.togglePlayPause() }
            DownloaderAction.OnSkipToPreviousClick -> viewModelScope.launch { musicPlayer.skipToPrevious() }
            DownloaderAction.OnSkipNextClick -> viewModelScope.launch { musicPlayer.skipToNext() }
            is DownloaderAction.OnSeek -> seek(action.fraction)
            // Navigation is handled by the Root.
            DownloaderAction.OnMiniPlayerClick -> Unit
        }
    }

    private fun setUrl(url: String) {
        savedStateHandle[KEY_URL] = url
        _state.update { it.copy(url = url) }
    }

    private fun resolve() {
        val url = _state.value.url.trim()
        if (!isYoutubeLink(url)) {
            viewModelScope.launch { eventChannel.send(DownloaderEvent.Error(DownloadError.INVALID_LINK.toUiText())) }
            return
        }
        // A new lookup replaces the one in flight; its results would be for the old link.
        resolveJob?.cancel()
        resolveJob = viewModelScope.launch {
            results.value = emptyList()
            _state.update { it.copy(isResolving = true) }
            linkResolver.resolve(url)
                .onSuccess { results.value = it }
                .onFailure { eventChannel.send(DownloaderEvent.Error(it.toUiText())) }
            _state.update { it.copy(isResolving = false) }
        }
    }

    private fun download(videoId: String) {
        val track = results.value.firstOrNull { it.videoId == videoId } ?: return
        if (track.isIn(libraryKeys)) return
        viewModelScope.launch { downloadQueue.enqueue(listOf(track)) }
    }

    private fun downloadAll() {
        val tracks = tracksToDownload(results.value, libraryKeys, statuses)
        if (tracks.isEmpty()) return
        viewModelScope.launch { downloadQueue.enqueue(tracks) }
    }

    private fun seek(fraction: Float) {
        val positionMillis = musicPlayer.playbackState.value.seekPositionFor(fraction) ?: return
        // Shown straight away, so the released seek bar doesn't jump back until the session catches up.
        _state.update { it.copy(nowPlaying = it.nowPlaying?.copy(positionMillis = positionMillis)) }
        viewModelScope.launch { musicPlayer.seekTo(positionMillis) }
    }

    /** One message per batch of failures among the results on screen, not one per song. */
    private suspend fun reportNewFailures(tracks: List<RemoteTrack>, statuses: Map<String, DownloadStatus>) {
        val previous = previousStatuses
        previousStatuses = statuses
        if (previous == null) return

        val newFailures = tracks.mapNotNull { track ->
            val status = statuses[track.videoId] as? DownloadStatus.Failed ?: return@mapNotNull null
            if (previous[track.videoId] is DownloadStatus.Failed) null else track to status.error
        }
        if (newFailures.isEmpty()) return
        eventChannel.send(
            DownloaderEvent.DownloadsFailed(
                count = newFailures.size,
                title = newFailures.singleOrNull()?.first?.title,
                reason = newFailures.first().second.toUiText(),
            ),
        )
    }

    private fun RemoteTrack.toUi(libraryKeys: Set<LibraryKey>, statuses: Map<String, DownloadStatus>) = DownloadTrackUi(
        videoId = videoId,
        title = title,
        artistName = artistName,
        thumbnailUrl = thumbnailUrl,
        downloadState = when {
            isIn(libraryKeys) -> SongDownloadState.Downloaded
            else -> when (val status = statuses[videoId]) {
                DownloadStatus.Queued -> SongDownloadState.Queued
                is DownloadStatus.Downloading -> SongDownloadState.Downloading(status.progress)
                // A failed song can be tried again.
                is DownloadStatus.Failed, null -> SongDownloadState.NotDownloaded
            }
        },
    )

    private companion object {
        const val KEY_URL = "url"
    }
}
