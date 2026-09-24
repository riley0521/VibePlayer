package com.rfcoding.vibeplayer.feature.downloader.presentation.fakes

import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadQueue
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadStatus
import com.rfcoding.vibeplayer.feature.downloader.domain.RemoteTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Enqueued tracks become Queued; tests move them on through [statusesMutable]. */
class FakeDownloadQueue : DownloadQueue {

    val statusesMutable = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    override val statuses = statusesMutable

    /** Every list passed to [enqueue], in call order. */
    val enqueuedBatches = mutableListOf<List<RemoteTrack>>()

    override suspend fun enqueue(tracks: List<RemoteTrack>) {
        enqueuedBatches += tracks
        statusesMutable.update { statuses -> statuses + tracks.associate { it.videoId to DownloadStatus.Queued } }
    }
}
