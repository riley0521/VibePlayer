package com.rfcoding.vibeplayer.feature.downloader.domain

/** Title and artist as they are written into the downloaded file. */
data class TrackMetadata(
    val title: String,
    val artistName: String?,
)

private const val TopicSuffix = " - Topic"

/**
 * YouTube Music's own [track] and [artist] fields win. Without them the video [title] and the
 * [channel] name stand in, minus the " - Topic" that auto-generated artist channels carry. Returns
 * null when there is no title at all.
 */
fun resolveTrackMetadata(
    title: String?,
    track: String?,
    artist: String?,
    channel: String?,
): TrackMetadata? {
    val resolvedTitle = track.clean() ?: title.clean() ?: return null
    val resolvedArtist = artist.clean() ?: channel.clean()?.removeSuffix(TopicSuffix).clean()
    return TrackMetadata(title = resolvedTitle, artistName = resolvedArtist)
}

private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
