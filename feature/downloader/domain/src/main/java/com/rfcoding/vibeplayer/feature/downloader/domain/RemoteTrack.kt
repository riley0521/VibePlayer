package com.rfcoding.vibeplayer.feature.downloader.domain

/**
 * One song a pasted link points to. [title] and [artistName] are what gets written into the MP3's
 * tags, so they are also what the library is matched against.
 */
data class RemoteTrack(
    val videoId: String,
    val url: String,
    val title: String,
    val artistName: String?,
    val thumbnailUrl: String?,
    val durationMillis: Long?,
)
