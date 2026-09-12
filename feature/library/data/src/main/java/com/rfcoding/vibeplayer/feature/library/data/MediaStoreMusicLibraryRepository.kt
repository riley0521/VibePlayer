package com.rfcoding.vibeplayer.feature.library.data

import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.core.domain.util.map
import com.rfcoding.vibeplayer.feature.library.domain.MusicLibraryRepository
import com.rfcoding.vibeplayer.feature.library.domain.MusicScanner
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import kotlin.time.Clock

/** Syncs what the MediaStore scanner finds into Room. */
class MediaStoreMusicLibraryRepository(
    private val scanner: MusicScanner,
    private val songDataSource: SongLocalDataSource,
    private val clock: Clock = Clock.System,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) : MusicLibraryRepository {

    /** A silent background scan and a manual one must not interleave their syncs. */
    private val scanMutex = Mutex()

    override suspend fun scanMusic(filters: ScanFilters): Result<Int, DataError.Local> {
        return scanMutex.withLock {
            val scanned = when (val result = scanner.scan(filters)) {
                is Result.Error -> return@withLock result
                is Result.Success -> result.data
            }

            // These fresh ids, flags and dates only stick for new songs: the sync keeps the ones
            // of songs already stored.
            val now = clock.now().toEpochMilliseconds()
            val songs = scanned.map { song ->
                Song(
                    id = newId(),
                    title = song.title,
                    artistName = song.artistName,
                    fileUri = song.fileUri,
                    imageUri = song.imageUri,
                    durationMillis = song.durationMillis,
                    isFavorite = false,
                    createdAt = now,
                )
            }
            songDataSource.syncScannedSongs(songs).map { songs.size }
        }
    }
}
