package com.rfcoding.vibeplayer.feature.downloader.data.ytdlp

import android.content.Context
import androidx.core.content.edit
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Owns the bundled yt-dlp: unpacks Python and FFmpeg on first use (a few seconds, once per app
 * version) and updates the yt-dlp script at most once a day, because YouTube changes break old
 * versions. The update only runs while nothing is executing, so a running download never has its
 * script swapped underneath it.
 */
class YoutubeDlEngine(
    context: Context,
    private val clock: Clock = Clock.System,
) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val setupMutex = Mutex()
    private val runningCount = AtomicInteger(0)
    private var isInitialized = false

    /**
     * Runs yt-dlp. Cancelling the caller interrupts the blocked thread, which kills the process.
     * [processId] must be unique among concurrent runs.
     */
    suspend fun execute(
        request: YoutubeDLRequest,
        processId: String,
        onProgress: ((percent: Float) -> Unit)? = null,
    ): YoutubeDLResponse {
        acquire()
        try {
            return runInterruptible(Dispatchers.IO) {
                YoutubeDL.execute(request, processId) { percent, _, _ -> onProgress?.invoke(percent) }
            }
        } finally {
            runningCount.decrementAndGet()
        }
    }

    /** The next run updates yt-dlp first, e.g. after an extraction failure. */
    fun markOutdated() {
        preferences.edit { remove(KEY_LAST_UPDATE) }
    }

    /** Initializes and maybe updates, then counts the caller as running, all under one lock. */
    private suspend fun acquire() = withContext(Dispatchers.IO) {
        setupMutex.withLock {
            if (!isInitialized) {
                YoutubeDL.init(appContext)
                FFmpeg.init(appContext)
                isInitialized = true
            }
            if (runningCount.get() == 0 && isUpdateDue()) {
                updateQuietly()
            }
            runningCount.incrementAndGet()
        }
    }

    private fun isUpdateDue(): Boolean {
        val lastUpdate = preferences.getLong(KEY_LAST_UPDATE, 0L)
        return clock.now().toEpochMilliseconds() - lastUpdate >= UpdateInterval.inWholeMilliseconds
    }

    /** Offline or rate-limited is fine: the bundled or last version keeps working, and it's retried later. */
    private fun updateQuietly() {
        try {
            YoutubeDL.updateYoutubeDL(appContext, YoutubeDL.UpdateChannel.STABLE)
            preferences.edit { putLong(KEY_LAST_UPDATE, clock.now().toEpochMilliseconds()) }
        } catch (_: YoutubeDLException) {
        } catch (_: RuntimeException) {
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "yt_dlp"
        const val KEY_LAST_UPDATE = "last_update_millis"
        val UpdateInterval = 24.hours
    }
}
