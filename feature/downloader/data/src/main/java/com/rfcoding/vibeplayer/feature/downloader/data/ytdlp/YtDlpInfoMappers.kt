package com.rfcoding.vibeplayer.feature.downloader.data.ytdlp

import com.rfcoding.vibeplayer.feature.downloader.domain.RemoteTrack
import com.rfcoding.vibeplayer.feature.downloader.domain.resolveTrackMetadata

/** Placeholders YouTube keeps in a playlist for videos that are gone; they can't be downloaded. */
private val RemovedVideoTitles = setOf("[Private video]", "[Deleted video]", "[Unavailable video]")

/** Big enough for the 64dp card at any density, small enough to load quickly. */
private const val PreferredThumbnailWidth = 320

/** A video's tracks are itself; a playlist's are its entries, minus removed ones. */
internal fun YtDlpInfoDto.toRemoteTracks(): List<RemoteTrack> = if (type == "playlist" || entries != null) {
    entries.orEmpty().mapNotNull { it?.toRemoteTrack() }
} else {
    listOfNotNull(toRemoteTrack())
}

internal fun YtDlpInfoDto.toRemoteTrack(): RemoteTrack? {
    val videoId = id?.takeIf { it.isNotBlank() } ?: return null
    if (title in RemovedVideoTitles) return null
    val metadata = resolveTrackMetadata(
        title = title,
        track = track,
        artist = artists?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: artist,
        channel = channel ?: uploader,
    ) ?: return null

    return RemoteTrack(
        videoId = videoId,
        url = webpageUrl ?: url?.takeIf { it.startsWith("http") } ?: "https://www.youtube.com/watch?v=$videoId",
        title = metadata.title,
        artistName = metadata.artistName,
        thumbnailUrl = pickThumbnail() ?: "https://i.ytimg.com/vi/$videoId/mqdefault.jpg",
        durationMillis = duration?.let { (it * 1000).toLong() },
    )
}

/** The smallest thumbnail at least [PreferredThumbnailWidth] wide, else the largest there is. */
private fun YtDlpInfoDto.pickThumbnail(): String? {
    val sized = thumbnails.orEmpty().filter { !it.url.isNullOrBlank() }
    val bigEnough = sized
        .filter { (it.width ?: 0) >= PreferredThumbnailWidth }
        .minByOrNull { it.width ?: 0 }
    return bigEnough?.url
        ?: thumbnail
        ?: sized.maxByOrNull { it.width ?: 0 }?.url
}
