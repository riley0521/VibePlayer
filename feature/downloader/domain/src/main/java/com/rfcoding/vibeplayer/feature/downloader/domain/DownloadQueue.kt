package com.rfcoding.vibeplayer.feature.downloader.domain

import kotlinx.coroutines.flow.Flow

/** Downloads songs one at a time, in the background, into the Music folder and the library. */
interface DownloadQueue {

    /** Keyed by [RemoteTrack.videoId]. */
    val statuses: Flow<Map<String, DownloadStatus>>

    /** Appends [tracks] to the queue, in order. A failed track that is enqueued again is retried. */
    suspend fun enqueue(tracks: List<RemoteTrack>)
}
