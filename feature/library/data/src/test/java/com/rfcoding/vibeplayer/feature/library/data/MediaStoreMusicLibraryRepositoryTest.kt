package com.rfcoding.vibeplayer.feature.library.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.library.domain.MinDuration
import com.rfcoding.vibeplayer.feature.library.domain.MinSize
import com.rfcoding.vibeplayer.feature.library.domain.MusicScanner
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import com.rfcoding.vibeplayer.feature.library.domain.ScannedSong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class MediaStoreMusicLibraryRepositoryTest {

    private lateinit var scanner: FakeMusicScanner
    private lateinit var songDataSource: FakeSongLocalDataSource
    private lateinit var repository: MediaStoreMusicLibraryRepository

    @BeforeEach
    fun setUp() {
        scanner = FakeMusicScanner()
        songDataSource = FakeSongLocalDataSource()
        var nextId = 0
        repository = MediaStoreMusicLibraryRepository(
            scanner = scanner,
            songDataSource = songDataSource,
            clock = FixedClock(epochMillis = 1_000),
            newId = { "id-${++nextId}" },
        )
    }

    @Test
    fun `scanned songs are synced as new songs and counted`() = runTest {
        scanner.result = Result.Success(
            listOf(
                ScannedSong("Intro", "Band", "content://1", "file://cover", 60_000),
                ScannedSong("Outro", null, "content://2", null, 90_000),
            ),
        )

        val result = repository.scanMusic(ScanFilters())

        assertThat(result).isEqualTo(Result.Success(2))
        assertThat(songDataSource.syncedSongs).containsExactly(
            listOf(
                Song("id-1", "Intro", "Band", "content://1", "file://cover", 60_000, isFavorite = false, createdAt = 1_000),
                Song("id-2", "Outro", null, "content://2", null, 90_000, isFavorite = false, createdAt = 1_000),
            ),
        )
    }

    @Test
    fun `the filters reach the scanner`() = runTest {
        val filters = ScanFilters(MinDuration.SixtySeconds, MinSize.FiveHundredKb)

        repository.scanMusic(filters)

        assertThat(scanner.receivedFilters).containsExactly(filters)
    }

    @Test
    fun `a failed scan leaves the stored songs untouched`() = runTest {
        scanner.result = Result.Error(DataError.Local.UNKNOWN)

        val result = repository.scanMusic(ScanFilters())

        assertThat(result).isEqualTo(Result.Error(DataError.Local.UNKNOWN))
        assertThat(songDataSource.syncedSongs).isEmpty()
    }

    @Test
    fun `a failed sync is reported`() = runTest {
        songDataSource.syncResult = Result.Error(DataError.Local.DISK_FULL)

        val result = repository.scanMusic(ScanFilters())

        assertThat(result).isEqualTo(Result.Error(DataError.Local.DISK_FULL))
    }

    private class FakeMusicScanner : MusicScanner {
        var result: Result<List<ScannedSong>, DataError.Local> = Result.Success(emptyList())
        val receivedFilters = mutableListOf<ScanFilters>()

        override suspend fun scan(filters: ScanFilters): Result<List<ScannedSong>, DataError.Local> {
            receivedFilters += filters
            return result
        }
    }

    private class FakeSongLocalDataSource : SongLocalDataSource {
        var syncResult: EmptyResult<DataError.Local> = Result.Success(Unit)
        val syncedSongs = mutableListOf<List<Song>>()

        override fun observeSongs(): Flow<List<Song>> = emptyFlow()
        override fun observeFavoriteSongs(): Flow<List<Song>> = emptyFlow()
        override suspend fun setFavorite(songId: String, isFavorite: Boolean) = Result.Success(Unit)

        override suspend fun syncScannedSongs(songs: List<Song>): EmptyResult<DataError.Local> {
            syncedSongs += songs
            return syncResult
        }
    }

    private class FixedClock(private val epochMillis: Long) : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(epochMillis)
    }
}
