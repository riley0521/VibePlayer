package com.rfcoding.vibeplayer.feature.downloader.data.queue

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rfcoding.vibeplayer.core.domain.util.Result as DomainResult
import com.rfcoding.vibeplayer.feature.downloader.data.library.DownloadedSongImporter
import com.rfcoding.vibeplayer.feature.downloader.data.library.MusicFolderWriter
import com.rfcoding.vibeplayer.feature.downloader.data.ytdlp.YoutubeDlEngine
import com.rfcoding.vibeplayer.feature.downloader.data.ytdlp.classifyYtDlpError
import com.rfcoding.vibeplayer.feature.downloader.data.ytdlp.downloadRequest
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import java.io.File

/** yt-dlp reports percentages; anything below 0 means it hasn't started. */
private const val PERCENT = 100f

/**
 * Drains [DownloadQueueStore] one track at a time: yt-dlp downloads and tags the MP3 in the cache,
 * [MusicFolderWriter] moves it into `Music/`, and [DownloadedSongImporter] adds it to the library.
 * A failed track is marked and skipped, so one bad video never stops the rest.
 */
class DownloadWorker(
    context: Context,
    params: WorkerParameters,
    private val store: DownloadQueueStore,
    private val engine: YoutubeDlEngine,
    private val writer: MusicFolderWriter,
    private val importer: DownloadedSongImporter,
    private val notifications: DownloadNotifications,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            while (true) {
                val next = store.nextPending() ?: break
                val remaining = store.pendingCount() - 1
                startForeground(next.title, remaining)
                store.setProgress(next.videoId, 0f)

                val error = download(next, remaining)
                if (error == null) {
                    store.complete(next.videoId)
                } else {
                    store.fail(next.videoId, error)
                }
                store.clearProgress()
            }
        } finally {
            // A stopped worker leaves its track waiting, and WorkManager runs it again later.
            store.clearProgress()
        }
        return Result.success()
    }

    override suspend fun getForegroundInfo() = notifications.foregroundInfo(title = null, remaining = 0, progress = null)

    /** Returns null on success. */
    private suspend fun download(entry: QueuedDownload, remaining: Int): DownloadError? {
        val outputDir = File(applicationContext.cacheDir, OUTPUT_DIR_NAME).apply { mkdirs() }
        try {
            var lastPercent = -1
            engine.execute(
                request = downloadRequest(entry.url, entry.videoId, entry.title, entry.artistName, outputDir),
                processId = "download-${entry.videoId}",
            ) { percent ->
                if (percent < 0) return@execute
                store.setProgress(entry.videoId, percent / PERCENT)
                if (percent.toInt() != lastPercent) {
                    lastPercent = percent.toInt()
                    notifications.update(entry.title, remaining, percent / PERCENT)
                }
            }

            val mp3 = File(outputDir, "${entry.videoId}.mp3")
            if (!mp3.exists()) return DownloadError.UNKNOWN

            val written = when (val result = writer.write(mp3, entry.title, entry.artistName, fallbackName = entry.videoId)) {
                is DomainResult.Error -> return result.error
                is DomainResult.Success -> result.data
            }
            return when (val imported = importer.import(written.uri.toString(), written.mediaId)) {
                is DomainResult.Error -> imported.error
                is DomainResult.Success -> null
            }
        } catch (e: YoutubeDLException) {
            val error = classifyYtDlpError(e.message)
            if (error == DownloadError.EXTRACTOR_FAILED) engine.markOutdated()
            return error
        } catch (_: YoutubeDL.CanceledException) {
            return DownloadError.UNKNOWN
        } finally {
            outputDir.listFiles { file -> file.name.startsWith(entry.videoId) }?.forEach { it.delete() }
        }
    }

    /**
     * Android 12+ refuses a foreground service started from the background. The worker's first
     * track starts from the user's tap, so this only fails for a worker WorkManager resumed later;
     * it then carries on as a regular background job.
     */
    private suspend fun startForeground(title: String, remaining: Int) {
        try {
            setForeground(notifications.foregroundInfo(title, remaining, progress = null))
        } catch (_: IllegalStateException) {
        }
    }

    private companion object {
        const val OUTPUT_DIR_NAME = "downloads"
    }
}
