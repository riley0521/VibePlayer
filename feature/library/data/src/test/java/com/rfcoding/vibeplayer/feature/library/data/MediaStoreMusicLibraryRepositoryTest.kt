package com.rfcoding.vibeplayer.feature.library.data

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.core.testing.FakeSongLocalDataSource
import com.rfcoding.vibeplayer.feature.library.domain.IncompleteScan
import com.rfcoding.vibeplayer.feature.library.domain.MinDuration
import com.rfcoding.vibeplayer.feature.library.domain.MinSize
import com.rfcoding.vibeplayer.feature.library.domain.MusicScanner
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import com.rfcoding.vibeplayer.feature.library.domain.ScannedSong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MediaStoreMusicLibraryRepositoryTest {

    private lateinit var scanner: FakeMusicScanner
    private lateinit var songDataSource: FakeSongLocalDataSource

    @BeforeEach
    fun setUp() {
        scanner = FakeMusicScanner()
        songDataSource = FakeSongLocalDataSource()
    }

    /** The scan runs in [TestScope.backgroundScope], standing in for the application scope. */
    private fun TestScope.createRepository(): MediaStoreMusicLibraryRepository {
        var nextId = 0
        return MediaStoreMusicLibraryRepository(
            scanner = scanner,
            songDataSource = songDataSource,
            applicationScope = backgroundScope,
            clock = FixedClock(epochMillis = 1_000),
            newId = { "id-${++nextId}" },
        )
    }

    @Test
    fun `scanned songs are stored as new songs and the scan returns after the first batch`() = runTest {
        val repository = createRepository()
        scanner.send(listOf(scanned(1, artistName = "Band", imageUri = "file://cover"), scanned(2)))

        val result = repository.scanMusic(ScanFilters())

        assertThat(result).isEqualTo(Result.Success(2))
        assertThat(songDataSource.upsertedBatches).containsExactly(
            listOf(
                Song("id-1", "Song 1", "Band", "content://1", "file://cover", 60_000, isFavorite = false, createdAt = 1_000),
                Song("id-2", "Song 2", null, "content://2", null, 60_000, isFavorite = false, createdAt = 1_000),
            ),
        )
        // The rest of the scan hasn't arrived, so nothing may be pruned yet.
        assertThat(songDataSource.prunedWith).isEmpty()
    }

    @Test
    fun `later batches sync in the background, then songs missing from the whole scan are pruned`() = runTest {
        val repository = createRepository()
        scanner.send(listOf(scanned(1)))
        repository.scanMusic(ScanFilters())

        scanner.send(listOf(scanned(2), scanned(3)))
        scanner.finish()
        runCurrent()

        assertThat(songDataSource.upsertedBatches).hasSize(2)
        assertThat(songDataSource.prunedWith).containsExactly(songDataSource.upsertedBatches.flatten())
    }

    @Test
    fun `the filters reach the scanner`() = runTest {
        val repository = createRepository()
        val filters = ScanFilters(MinDuration.SixtySeconds, MinSize.FiveHundredKb)
        scanner.finish()

        repository.scanMusic(filters)

        assertThat(scanner.receivedFilters).containsExactly(filters)
    }

    @Test
    fun `a scan that finds nothing prunes before it returns`() = runTest {
        val repository = createRepository()
        scanner.send(emptyList())
        scanner.finish()

        val result = repository.scanMusic(ScanFilters())

        assertThat(result).isEqualTo(Result.Success(0))
        assertThat(songDataSource.prunedWith).containsExactly(emptyList<Song>())
    }

    @Test
    fun `a failed scan leaves the stored songs untouched`() = runTest {
        val repository = createRepository()
        scanner.send(Result.Error(DataError.Local.UNKNOWN))
        scanner.finish()

        val result = repository.scanMusic(ScanFilters())

        assertThat(result).isEqualTo(Result.Error(DataError.Local.UNKNOWN))
        assertThat(songDataSource.upsertedBatches).isEmpty()
        assertThat(songDataSource.prunedWith).isEmpty()
    }

    @Test
    fun `a failed first batch is returned and nothing is pruned`() = runTest {
        val repository = createRepository()
        songDataSource.upsertError = DataError.Local.DISK_FULL
        scanner.send(listOf(scanned(1)))
        scanner.send(listOf(scanned(2)))
        scanner.finish()

        val result = repository.scanMusic(ScanFilters())
        runCurrent()

        assertThat(result).isEqualTo(Result.Error(DataError.Local.DISK_FULL))
        assertThat(songDataSource.upsertedBatches).hasSize(1)
        assertThat(songDataSource.prunedWith).isEmpty()
    }

    @Test
    fun `failed later batches are counted as skipped, the rest still sync and nothing is pruned`() = runTest {
        val repository = createRepository()
        scanner.send(listOf(scanned(1)))
        repository.scanMusic(ScanFilters())

        repository.incompleteScans.test {
            songDataSource.upsertError = DataError.Local.DISK_FULL
            scanner.send(listOf(scanned(2), scanned(3)))
            scanner.send(listOf(scanned(4)))
            scanner.finish()

            assertThat(awaitItem()).isEqualTo(IncompleteScan(skippedSongCount = 3, error = DataError.Local.DISK_FULL))
        }
        assertThat(songDataSource.upsertedBatches).hasSize(3)
        assertThat(songDataSource.prunedWith).isEmpty()
    }

    @Test
    fun `a scanner failure after the first batch is reported and nothing is pruned`() = runTest {
        val repository = createRepository()
        scanner.send(listOf(scanned(1)))
        repository.scanMusic(ScanFilters())

        repository.incompleteScans.test {
            scanner.send(Result.Error(DataError.Local.UNKNOWN))
            scanner.finish()

            assertThat(awaitItem()).isEqualTo(IncompleteScan(skippedSongCount = 0, error = DataError.Local.UNKNOWN))
        }
        assertThat(songDataSource.prunedWith).isEmpty()
    }

    @Test
    fun `a failed prune after the first batch is reported`() = runTest {
        val repository = createRepository()
        songDataSource.pruneError = DataError.Local.UNKNOWN
        scanner.send(listOf(scanned(1)))
        repository.scanMusic(ScanFilters())

        repository.incompleteScans.test {
            scanner.finish()

            assertThat(awaitItem()).isEqualTo(IncompleteScan(skippedSongCount = 0, error = DataError.Local.UNKNOWN))
        }
    }

    @Test
    fun `a new scan cancels the running one, which never prunes`() = runTest {
        val repository = createRepository()
        scanner.send(listOf(scanned(1)))
        repository.scanMusic(ScanFilters())

        scanner.restart()
        scanner.send(listOf(scanned(2)))
        scanner.finish()
        repository.scanMusic(ScanFilters(minDuration = MinDuration.SixtySeconds))
        runCurrent()

        // Only the second scan ran to the end.
        assertThat(songDataSource.prunedWith).containsExactly(listOf(songDataSource.upsertedBatches.last().single()))
    }

    @Test
    fun `a caller still waiting on a cancelled scan gets the new scan's result`() = runTest {
        val repository = createRepository()
        val firstCall = async { repository.scanMusic(ScanFilters()) }
        runCurrent()

        scanner.restart()
        scanner.send(listOf(scanned(1)))
        val secondResult = repository.scanMusic(ScanFilters())

        assertThat(secondResult).isEqualTo(Result.Success(1))
        assertThat(firstCall.await()).isEqualTo(Result.Success(1))
    }

    private fun scanned(
        number: Int,
        artistName: String? = null,
        imageUri: String? = null,
    ) = ScannedSong("Song $number", artistName, "content://$number", imageUri, 60_000)

    /** Hands a scan the batches a test sends, until the test finishes it. */
    private class FakeMusicScanner : MusicScanner {
        private var batches = newChannel()
        val receivedFilters = mutableListOf<ScanFilters>()

        override fun scan(filters: ScanFilters): Flow<Result<List<ScannedSong>, DataError.Local>> {
            val scanBatches = batches
            return flow {
                receivedFilters += filters
                emitAll(scanBatches)
            }
        }

        fun send(songs: List<ScannedSong>) = send(Result.Success(songs))

        fun send(batch: Result<List<ScannedSong>, DataError.Local>) {
            batches.trySend(batch)
        }

        fun finish() {
            batches.close()
        }

        /** Batches sent from now on go to the next scan; the running one waits forever. */
        fun restart() {
            batches = newChannel()
        }

        private fun newChannel() = Channel<Result<List<ScannedSong>, DataError.Local>>(Channel.UNLIMITED)
    }

    private class FixedClock(private val epochMillis: Long) : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(epochMillis)
    }
}
