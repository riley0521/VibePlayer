package com.rfcoding.vibeplayer.feature.downloader.presentation.downloader

import com.rfcoding.vibeplayer.core.presentation.UiText

sealed interface DownloaderEvent {
    data class Error(val message: UiText) : DownloaderEvent

    /**
     * Downloads that just failed. [title] is set when there's one, so the message can name it;
     * [reason] is the first failure's.
     */
    data class DownloadsFailed(val count: Int, val title: String?, val reason: UiText) : DownloaderEvent
}
