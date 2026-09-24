package com.rfcoding.vibeplayer.feature.downloader.presentation.downloader

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.rfcoding.vibeplayer.core.designsystem.components.SongDownloadState
import com.rfcoding.vibeplayer.core.presentation.NowPlayingUi

@Stable
data class DownloaderState(
    val url: String = "",
    val isResolving: Boolean = false,
    val tracks: List<DownloadTrackUi> = emptyList(),
    /** More than one result and at least one of them still to download. */
    val canDownloadAll: Boolean = false,
    val nowPlaying: NowPlayingUi? = null,
)

@Immutable
data class DownloadTrackUi(
    val videoId: String,
    val title: String,
    val artistName: String?,
    val thumbnailUrl: String?,
    val downloadState: SongDownloadState,
)
