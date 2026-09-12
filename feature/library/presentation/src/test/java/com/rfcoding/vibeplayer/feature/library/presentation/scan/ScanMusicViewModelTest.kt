package com.rfcoding.vibeplayer.feature.library.presentation.scan

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.library.domain.MinDuration
import com.rfcoding.vibeplayer.feature.library.domain.MinSize
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import com.rfcoding.vibeplayer.feature.library.presentation.fakes.FakeMusicLibraryRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanMusicViewModelTest {

    private lateinit var repository: FakeMusicLibraryRepository
    private lateinit var viewModel: ScanMusicViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeMusicLibraryRepository()
        viewModel = ScanMusicViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts on the silent scan's filters`() {
        val state = viewModel.state.value

        assertThat(ScanFilters(state.minDuration, state.minSize)).isEqualTo(ScanFilters())
    }

    @Test
    fun `scanning uses the selected filters and navigates back when done`() = runTest {
        viewModel.onAction(ScanMusicAction.OnMinDurationSelect(MinDuration.SixtySeconds))
        viewModel.onAction(ScanMusicAction.OnMinSizeSelect(MinSize.FiveHundredKb))

        viewModel.events.test {
            viewModel.onAction(ScanMusicAction.OnScanClick)

            assertThat(awaitItem()).isEqualTo(ScanMusicEvent.NavigateBack)
        }
        assertThat(repository.receivedFilters).containsExactly(
            ScanFilters(MinDuration.SixtySeconds, MinSize.FiveHundredKb),
        )
    }

    @Test
    fun `the button shows the loader while scanning and ignores more taps`() = runTest {
        repository.gate = CompletableDeferred()

        viewModel.onAction(ScanMusicAction.OnScanClick)
        viewModel.onAction(ScanMusicAction.OnScanClick)

        assertThat(viewModel.state.value.isScanning).isTrue()
        assertThat(repository.receivedFilters.size).isEqualTo(1)
    }

    @Test
    fun `a failed scan sends an error and stays on the screen`() = runTest {
        repository.result = Result.Error(DataError.Local.DISK_FULL)

        viewModel.events.test {
            viewModel.onAction(ScanMusicAction.OnScanClick)

            assertThat(awaitItem()).isInstanceOf<ScanMusicEvent.Error>()
        }
        assertThat(viewModel.state.value.isScanning).isFalse()
    }
}
