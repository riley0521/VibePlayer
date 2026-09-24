package com.rfcoding.vibeplayer.feature.downloader.domain

/** A song the download queue still knows about. A finished download drops out: the library has it now. */
sealed interface DownloadStatus {
    data object Queued : DownloadStatus
    data class Downloading(val progress: Float) : DownloadStatus
    data class Failed(val error: DownloadError) : DownloadStatus
}
