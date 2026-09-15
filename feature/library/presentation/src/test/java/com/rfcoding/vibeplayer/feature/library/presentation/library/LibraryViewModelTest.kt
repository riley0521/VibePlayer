package com.rfcoding.vibeplayer.feature.library.presentation.library

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.rfcoding.vibeplayer.core.domain.player.PlaybackState
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.core.presentation.toSongUi
import com.rfcoding.vibeplayer.core.testing.FakeMusicPlayer
import com.rfcoding.vibeplayer.core.testing.FakeSongLocalDataSource
import com.rfcoding.vibeplayer.core.testing.song
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import com.rfcoding.vibeplayer.feature.library.presentation.fakes.FakeMusicLibraryRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private lateinit var repository: FakeMusicLibraryRepository
    private lateinit var songDataSource: FakeSongLocalDataSource
    private lateinit var musicPlayer: FakeMusicPlayer

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeMusicLibraryRepository()
        songDataSource = FakeSongLocalDataSource()
        musicPlayer = FakeMusicPlayer()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LibraryViewModel(repository, songDataSource, musicPlayer)

    @Test
    fun `an empty library shows Scanning while the first scan runs`() = runTest {
        repository.gate = CompletableDeferred()

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.status).isEqualTo(LibraryStatus.Scanning)
        assertThat(repository.receivedFilters).containsExactly(ScanFilters())
    }

    @Test
    fun `an empty library shows No music found when the scan finds nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.state.value.status).isEqualTo(LibraryStatus.NoMusicFound)
    }

    @Test
    fun `an empty library shows the songs the first scan finds`() = runTest {
        repository.result = Result.Success(2)
        repository.onScanSuccess = { songDataSource.songsMutable.value = listOf(song("a"), song("b")) }

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.status).isEqualTo(LibraryStatus.Loaded)
    }

    @Test
    fun `a library with songs shows them at once and rescans silently with the default filters`() = runTest {
        songDataSource.songsMutable.value = listOf(song("a"))
        repository.gate = CompletableDeferred()

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.status).isEqualTo(LibraryStatus.Loaded)
        assertThat(repository.receivedFilters).containsExactly(ScanFilters())
    }

    @Test
    fun `a failed silent scan sends no error`() = runTest {
        songDataSource.songsMutable.value = listOf(song("a"))
        repository.result = Result.Error(DataError.Local.UNKNOWN)

        val viewModel = createViewModel()

        viewModel.events.test {
            expectNoEvents()
        }
        assertThat(viewModel.state.value.status).isEqualTo(LibraryStatus.Loaded)
    }

    @Test
    fun `a failed first scan sends an error and shows No music found`() = runTest {
        repository.result = Result.Error(DataError.Local.UNKNOWN)

        val viewModel = createViewModel()

        viewModel.events.test {
            assertThat(awaitItem()).isInstanceOf<LibraryEvent.Error>()
        }
        advanceUntilIdle()
        assertThat(viewModel.state.value.status).isEqualTo(LibraryStatus.NoMusicFound)
    }

    @Test
    fun `scan again shows Scanning and scans once more`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        repository.gate = CompletableDeferred()

        viewModel.onAction(LibraryAction.OnScanAgainClick)

        assertThat(viewModel.state.value.status).isEqualTo(LibraryStatus.Scanning)
        assertThat(repository.receivedFilters).hasSize(2)
    }

    @Test
    fun `selecting a tab updates the state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(LibraryAction.OnTabSelect(LibraryTab.Playlist))

        assertThat(viewModel.state.value.selectedTab).isEqualTo(LibraryTab.Playlist)
    }

    @Test
    fun `the mini player is hidden while nothing is queued`() = runTest {
        val viewModel = createViewModel()

        assertThat(viewModel.state.value.nowPlaying).isNull()
    }

    @Test
    fun `the mini player follows the playback state`() = runTest {
        val queue = listOf(song("a"), song("b"), song("c"))
        val viewModel = createViewModel()

        musicPlayer.playbackState.value = PlaybackState(
            queue = queue,
            currentIndex = 1,
            isPlaying = true,
            positionMillis = 12_000,
        )

        assertThat(viewModel.state.value.nowPlaying).isEqualTo(
            NowPlayingUi(
                song = queue[1].toSongUi(),
                isPlaying = true,
                positionMillis = 12_000,
                canSkipToPrevious = true,
            ),
        )
    }

    @Test
    fun `the first song of the queue can't skip to previous`() = runTest {
        val viewModel = createViewModel()

        musicPlayer.playbackState.value = PlaybackState(queue = listOf(song("a"), song("b")), currentIndex = 0)

        assertThat(viewModel.state.value.nowPlaying?.canSkipToPrevious).isEqualTo(false)
    }

    @Test
    fun `the first song can skip to previous when the queue repeats`() = runTest {
        val viewModel = createViewModel()

        musicPlayer.playbackState.value = PlaybackState(
            queue = listOf(song("a"), song("b")),
            currentIndex = 0,
            repeatMode = RepeatMode.All,
        )

        assertThat(viewModel.state.value.nowPlaying?.canSkipToPrevious).isEqualTo(true)
    }

    @Test
    fun `mini player buttons control the player`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(LibraryAction.OnPlayPauseClick)
        viewModel.onAction(LibraryAction.OnSkipNextClick)
        viewModel.onAction(LibraryAction.OnSkipNextClick)
        viewModel.onAction(LibraryAction.OnSkipToPreviousClick)

        assertThat(musicPlayer.togglePlayPauseCount).isEqualTo(1)
        assertThat(musicPlayer.skipToNextCount).isEqualTo(2)
        assertThat(musicPlayer.skipToPreviousCount).isEqualTo(1)
    }

    @Test
    fun `seeking the mini player moves the song and shows the new position at once`() = runTest {
        val viewModel = createViewModel()
        musicPlayer.playbackState.value = PlaybackState(queue = listOf(song("a")), currentIndex = 0)

        viewModel.onAction(LibraryAction.OnSeek(0.25f))

        assertThat(musicPlayer.seekPositions).containsExactly(15_000L)
        assertThat(viewModel.state.value.nowPlaying?.positionMillis).isEqualTo(15_000L)
    }

    @Test
    fun `seeking does nothing while nothing is queued`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(LibraryAction.OnSeek(0.5f))

        assertThat(musicPlayer.seekPositions).isEmpty()
    }
}
