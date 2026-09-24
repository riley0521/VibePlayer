package com.rfcoding.vibeplayer.feature.downloader.presentation.downloader

sealed interface DownloaderAction {
    data class OnUrlChange(val url: String) : DownloaderAction
    data object OnClearClick : DownloaderAction
    data object OnFindClick : DownloaderAction
    /** The Paste button: the clipboard's text replaces the link and is looked up straight away. */
    data class OnPaste(val text: String) : DownloaderAction
    data class OnDownloadClick(val videoId: String) : DownloaderAction
    data object OnDownloadAllClick : DownloaderAction

    data object OnMiniPlayerClick : DownloaderAction
    data object OnPlayPauseClick : DownloaderAction
    data object OnSkipToPreviousClick : DownloaderAction
    data object OnSkipNextClick : DownloaderAction
    data class OnSeek(val fraction: Float) : DownloaderAction
}
