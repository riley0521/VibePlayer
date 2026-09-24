package com.rfcoding.vibeplayer.feature.downloader.data.queue

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadQueue
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadStatus
import com.rfcoding.vibeplayer.feature.downloader.domain.RemoteTrack
import kotlinx.coroutines.flow.Flow

/**
 * Stores the tracks in [DownloadQueueStore] and makes sure a [DownloadWorker] is draining it. One
 * worker downloads every waiting track in turn, so the foreground service it starts while the user
 * is in the app keeps running in the background until the queue is empty.
 */
class WorkManagerDownloadQueue(
    private val store: DownloadQueueStore,
    private val workManager: WorkManager,
) : DownloadQueue {

    override val statuses: Flow<Map<String, DownloadStatus>> = store.statuses

    override suspend fun enqueue(tracks: List<RemoteTrack>) {
        if (tracks.isEmpty()) return
        store.add(tracks.map { it.toQueuedDownload() })
        // A worker that is about to finish may miss these, so another one runs after it; if it
        // finds the queue empty it ends at once.
        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            OneTimeWorkRequestBuilder<DownloadWorker>()
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .build(),
        )
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "song_downloads"
    }
}
