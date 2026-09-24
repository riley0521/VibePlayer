package com.rfcoding.vibeplayer.feature.downloader.presentation.downloader

import com.rfcoding.vibeplayer.core.presentation.UiText
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import com.rfcoding.vibeplayer.feature.downloader.presentation.R

fun DownloadError.toUiText(): UiText {
    val id = when (this) {
        DownloadError.NO_INTERNET -> R.string.error_no_internet
        DownloadError.INVALID_LINK -> R.string.error_invalid_link
        DownloadError.UNAVAILABLE -> R.string.error_unavailable
        DownloadError.EXTRACTOR_FAILED -> R.string.error_extractor_failed
        DownloadError.DISK_FULL -> R.string.error_disk_full
        DownloadError.STORAGE_PERMISSION_DENIED -> R.string.error_storage_permission
        DownloadError.UNKNOWN -> R.string.error_unknown
    }
    return UiText.StringResource(id)
}
