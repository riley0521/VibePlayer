package com.rfcoding.vibeplayer.feature.downloader.domain

import com.rfcoding.vibeplayer.core.domain.util.Error

/** Failures of looking up a link or downloading a song. Only the downloader touches the network. */
enum class DownloadError : Error {
    NO_INTERNET,
    INVALID_LINK,
    UNAVAILABLE,
    EXTRACTOR_FAILED,
    DISK_FULL,
    STORAGE_PERMISSION_DENIED,
    UNKNOWN,
}
