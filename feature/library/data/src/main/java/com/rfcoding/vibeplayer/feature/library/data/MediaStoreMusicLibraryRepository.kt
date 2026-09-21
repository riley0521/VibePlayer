package com.rfcoding.vibeplayer.feature.library.data

import android.util.Log
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.library.domain.IncompleteScan
import com.rfcoding.vibeplayer.feature.library.domain.MusicLibraryRepository
import com.rfcoding.vibeplayer.feature.library.domain.MusicScanner
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import com.rfcoding.vibeplayer.feature.library.domain.ScannedSong
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import kotlin.time.Clock

/**
 * Syncs what the MediaStore scanner finds into Room, one batch at a time. The scan runs in
 * [applicationScope] so its later batches outlive the screen that started it.
 */
class MediaStoreMusicLibraryRepository(
    private val scanner: MusicScanner,
    private val songDataSource: SongLocalDataSource,
    private val applicationScope: CoroutineScope,
    private val clock: Clock = Clock.System,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) : MusicLibraryRepository {

    private val _incompleteScans = MutableSharedFlow<IncompleteScan>(extraBufferCapacity = 1)
    override val incompleteScans: SharedFlow<IncompleteScan> = _incompleteScans.asSharedFlow()

    /** Two scans started together must not both replace [currentScan]. */
    private val scanMutex = Mutex()
    private var currentScan: RunningScan? = null

    override suspend fun scanMusic(filters: ScanFilters): Result<Int, DataError.Local> {
        val firstResult = scanMutex.withLock {
            val previous = currentScan
            previous?.job?.cancelAndJoin()

            val firstResult = CompletableDeferred<Result<Int, DataError.Local>>()
            // Whoever still waits on the cancelled scan gets this scan's answer instead.
            previous?.firstResult
                ?.takeIf { !it.isCompleted }
                ?.let { stale -> firstResult.invokeOnCompletion { stale.complete(firstResult.getCompleted()) } }

            val job = applicationScope.launch { runScan(filters, firstResult) }
            currentScan = RunningScan(job, firstResult)
            firstResult
        }
        return firstResult.await()
    }

    /**
     * Upserts every batch, completing [firstResult] once one holding songs is stored, then prunes
     * the songs the scan no longer found. Pruning is skipped after any failure, so an incomplete
     * scan never deletes a song.
     */
    private suspend fun runScan(
        filters: ScanFilters,
        firstResult: CompletableDeferred<Result<Int, DataError.Local>>,
    ) {
        // These fresh ids, flags and dates only stick for new songs: the upsert keeps the ones
        // of songs already stored.
        val now = clock.now().toEpochMilliseconds()
        val stored = mutableListOf<Song>()
        var skippedCount = 0
        var error: DataError.Local? = null
        var temp = System.currentTimeMillis()

        // takeWhile stops the scanner as soon as a batch says the scan can't go on.
        scanner.scan(filters).takeWhile { batch ->
            // For debugging purposes only.
            // temp = measureTimeAndLog(temp)

            val songs = when (batch) {
                is Result.Error -> {
                    error = batch.error
                    return@takeWhile false
                }
                is Result.Success -> batch.data.map { it.toSong(now) }
            }
            when (val upsert = songDataSource.upsertScannedSongs(songs)) {
                is Result.Success -> {
                    stored += songs
                    if (songs.isNotEmpty()) {
                        firstResult.complete(Result.Success(stored.size))
                    }
                    true
                }
                is Result.Error -> {
                    error = upsert.error
                    // Before the first result nothing is stored yet, so fail like a whole scan.
                    // After it, keep storing the remaining batches and count what was lost.
                    skippedCount += songs.size
                    firstResult.isCompleted
                }
            }
        }.collect()

        val scanError = error
        if (scanError != null) {
            reportFailure(firstResult, IncompleteScan(skippedCount, scanError))
            return
        }
        when (val prune = songDataSource.pruneSongsMissingFrom(stored)) {
            is Result.Success -> firstResult.complete(Result.Success(stored.size))
            is Result.Error -> reportFailure(firstResult, IncompleteScan(0, prune.error))
        }
    }

    private fun measureTimeAndLog(temp: Long): Long {
        val millis = System.currentTimeMillis() - temp
        Log.d("MediaStoreMusicLibraryRepository", "Time taken: ${millis}ms")
        return System.currentTimeMillis()
    }

    /** Returns the error to the caller while it still waits, otherwise reports it in the background. */
    private suspend fun reportFailure(
        firstResult: CompletableDeferred<Result<Int, DataError.Local>>,
        failure: IncompleteScan,
    ) {
        if (!firstResult.complete(Result.Error(failure.error))) {
            _incompleteScans.emit(failure)
        }
    }

    private fun ScannedSong.toSong(now: Long) = Song(
        id = newId(),
        title = title,
        artistName = artistName,
        fileUri = fileUri,
        imageUri = imageUri,
        durationMillis = durationMillis,
        isFavorite = false,
        createdAt = now,
    )

    private class RunningScan(
        val job: Job,
        val firstResult: CompletableDeferred<Result<Int, DataError.Local>>,
    )
}
