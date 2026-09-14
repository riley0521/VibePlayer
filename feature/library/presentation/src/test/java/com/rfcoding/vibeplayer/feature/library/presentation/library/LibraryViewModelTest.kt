package com.rfcoding.vibeplayer.feature.library.presentation.library

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import com.rfcoding.vibeplayer.feature.library.presentation.fakes.FakeMusicLibraryRepository
import com.rfcoding.vibeplayer.feature.library.presentation.fakes.FakeSongLocalDataSource
import com.rfcoding.vibeplayer.feature.library.presentation.fakes.song
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

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeMusicLibraryRepository()
        songDataSource = FakeSongLocalDataSource()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LibraryViewModel(repository, songDataSource)

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
}
