package com.rfcoding.vibeplayer.feature.downloader.data.ytdlp

import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError

private val DiskFullMarkers = listOf("No space left on device", "Errno 28", "ENOSPC")
private val NetworkMarkers = listOf(
    "Unable to download webpage",
    "Unable to download API page",
    "Failed to resolve",
    "Temporary failure in name resolution",
    "No address associated with hostname",
    "Network is unreachable",
    "Connection refused",
    "Connection reset",
    "timed out",
    "getaddrinfo failed",
)
private val InvalidLinkMarkers = listOf("Unsupported URL", "is not a valid URL", "Incomplete YouTube ID")
private val BotCheckMarkers = listOf("confirm you're not a bot", "confirm you’re not a bot")
private val UnavailableMarkers = listOf(
    "Video unavailable",
    "Private video",
    "This video is not available",
    "This video has been removed",
    "members-only",
    "Sign in to confirm your age",
    "The playlist does not exist",
    "This playlist is private",
)

/**
 * Maps yt-dlp's stderr to what the user is told. Order matters: a network failure mentions the URL,
 * and YouTube's bot check reads like an unavailable video.
 */
internal fun classifyYtDlpError(stderr: String?): DownloadError {
    val text = stderr.orEmpty()
    fun matches(markers: List<String>) = markers.any { text.contains(it, ignoreCase = true) }
    return when {
        matches(DiskFullMarkers) -> DownloadError.DISK_FULL
        matches(NetworkMarkers) -> DownloadError.NO_INTERNET
        matches(InvalidLinkMarkers) -> DownloadError.INVALID_LINK
        matches(BotCheckMarkers) -> DownloadError.EXTRACTOR_FAILED
        matches(UnavailableMarkers) -> DownloadError.UNAVAILABLE
        text.contains("ERROR:") -> DownloadError.EXTRACTOR_FAILED
        else -> DownloadError.UNKNOWN
    }
}
