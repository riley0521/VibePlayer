package com.rfcoding.vibeplayer.feature.downloader.data.queue

import android.content.Context
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadStatus
import com.rfcoding.vibeplayer.feature.downloader.domain.RemoteTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

@Serializable
internal data class QueuedDownload(
    val videoId: String,
    val url: String,
    val title: String,
    val artistName: String? = null,
    /** Set once the download failed; the entry then waits for the user to retry it. */
    val error: DownloadError? = null,
)

internal fun RemoteTrack.toQueuedDownload() = QueuedDownload(
    videoId = videoId,
    url = url,
    title = title,
    artistName = artistName,
)

private data class RunningDownload(val videoId: String, val progress: Float)

/**
 * The download queue, kept in a small JSON file so a download the system interrupted, or one still
 * waiting when the process died, is picked up again by the next worker run. Progress lives in
 * memory only. Failures are forgotten on the next launch, since the results they belong to are gone.
 */
class DownloadQueueStore(
    context: Context,
) {
    private val file = File(context.applicationContext.filesDir, FILE_NAME)
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    /** Null until loaded from [file]. */
    private val entries = MutableStateFlow<List<QueuedDownload>?>(null)
    private val running = MutableStateFlow<RunningDownload?>(null)

    val statuses: Flow<Map<String, DownloadStatus>> = combine(entries.filterNotNull(), running) { entries, running ->
        entries.associate { entry ->
            entry.videoId to when {
                entry.error != null -> DownloadStatus.Failed(entry.error)
                running?.videoId == entry.videoId -> DownloadStatus.Downloading(running.progress)
                else -> DownloadStatus.Queued
            }
        }
    }.onStart { mutex.withLock { load() } }

    /** Appends [tracks]; a track already waiting keeps its place, a failed one is queued again. */
    internal suspend fun add(tracks: List<QueuedDownload>) = edit { current ->
        val waitingIds = current.filter { it.error == null }.mapTo(HashSet()) { it.videoId }
        val added = tracks.filter { it.videoId !in waitingIds }.distinctBy { it.videoId }
        val addedIds = added.mapTo(HashSet()) { it.videoId }
        current.filterNot { it.videoId in addedIds } + added
    }

    internal suspend fun nextPending(): QueuedDownload? = mutex.withLock {
        load().firstOrNull { it.error == null }
    }

    internal suspend fun pendingCount(): Int = mutex.withLock {
        load().count { it.error == null }
    }

    internal fun setProgress(videoId: String, progress: Float) {
        running.value = RunningDownload(videoId, progress.coerceIn(0f, 1f))
    }

    internal fun clearProgress() {
        running.value = null
    }

    internal suspend fun complete(videoId: String) = edit { current -> current.filterNot { it.videoId == videoId } }

    internal suspend fun fail(videoId: String, error: DownloadError) = edit { current ->
        current.map { if (it.videoId == videoId) it.copy(error = error) else it }
    }

    private suspend fun edit(transform: (List<QueuedDownload>) -> List<QueuedDownload>) = mutex.withLock {
        val updated = transform(load())
        entries.value = updated
        save(updated.filter { it.error == null })
    }

    /** Must hold [mutex]. */
    private suspend fun load(): List<QueuedDownload> {
        entries.value?.let { return it }
        val loaded = withContext(Dispatchers.IO) {
            try {
                if (file.exists()) json.decodeFromString<List<QueuedDownload>>(file.readText()) else emptyList()
            } catch (_: IOException) {
                emptyList()
            } catch (_: SerializationException) {
                emptyList()
            } catch (_: IllegalArgumentException) {
                emptyList()
            }
        }.filter { it.error == null }
        entries.update { it ?: loaded }
        return entries.value.orEmpty()
    }

    private suspend fun save(entries: List<QueuedDownload>) = withContext(Dispatchers.IO) {
        try {
            file.writeText(json.encodeToString(entries))
        } catch (_: IOException) {
            // Only resuming after a process death needs the file; the queue itself is in memory.
        }
    }

    private companion object {
        const val FILE_NAME = "download_queue.json"
    }
}
